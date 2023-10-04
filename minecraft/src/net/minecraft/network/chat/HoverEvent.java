package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HoverEvent {
	public static final Codec<HoverEvent> CODEC = Codec.either(HoverEvent.TypedHoverEvent.CODEC.codec(), HoverEvent.TypedHoverEvent.LEGACY_CODEC.codec())
		.xmap(
			either -> new HoverEvent(either.map(typedHoverEvent -> typedHoverEvent, typedHoverEvent -> typedHoverEvent)), hoverEvent -> Either.left(hoverEvent.event)
		);
	private final HoverEvent.TypedHoverEvent<?> event;

	public <T> HoverEvent(HoverEvent.Action<T> action, T object) {
		this(new HoverEvent.TypedHoverEvent<>(action, object));
	}

	private HoverEvent(HoverEvent.TypedHoverEvent<?> typedHoverEvent) {
		this.event = typedHoverEvent;
	}

	public HoverEvent.Action<?> getAction() {
		return this.event.action;
	}

	@Nullable
	public <T> T getValue(HoverEvent.Action<T> action) {
		return this.event.action == action ? action.cast(this.event.value) : null;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object != null && this.getClass() == object.getClass() ? ((HoverEvent)object).event.equals(this.event) : false;
		}
	}

	public String toString() {
		return this.event.toString();
	}

	public int hashCode() {
		return this.event.hashCode();
	}

	public static class Action<T> implements StringRepresentable {
		public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>("show_text", true, ComponentSerialization.CODEC, DataResult::success);
		public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>(
			"show_item", true, HoverEvent.ItemStackInfo.CODEC, HoverEvent.ItemStackInfo::legacyCreate
		);
		public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>(
			"show_entity", true, HoverEvent.EntityTooltipInfo.CODEC, HoverEvent.EntityTooltipInfo::legacyCreate
		);
		public static final Codec<HoverEvent.Action<?>> UNSAFE_CODEC = StringRepresentable.fromValues(
			() -> new HoverEvent.Action[]{SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY}
		);
		public static final Codec<HoverEvent.Action<?>> CODEC = ExtraCodecs.validate(UNSAFE_CODEC, HoverEvent.Action::filterForSerialization);
		private final String name;
		private final boolean allowFromServer;
		final Codec<HoverEvent.TypedHoverEvent<T>> codec;
		final Codec<HoverEvent.TypedHoverEvent<T>> legacyCodec;

		public Action(String string, boolean bl, Codec<T> codec, Function<Component, DataResult<T>> function) {
			this.name = string;
			this.allowFromServer = bl;
			this.codec = codec.<HoverEvent.TypedHoverEvent<T>>xmap(object -> new HoverEvent.TypedHoverEvent<>(this, (T)object), typedHoverEvent -> typedHoverEvent.value)
				.fieldOf("contents")
				.codec();
			this.legacyCodec = Codec.of(
				Encoder.error("Can't encode in legacy format"),
				ComponentSerialization.CODEC.flatMap(function).map(object -> new HoverEvent.TypedHoverEvent<>(this, (T)object))
			);
		}

		public boolean isAllowedFromServer() {
			return this.allowFromServer;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		T cast(Object object) {
			return (T)object;
		}

		public String toString() {
			return "<action " + this.name + ">";
		}

		private static DataResult<HoverEvent.Action<?>> filterForSerialization(@Nullable HoverEvent.Action<?> action) {
			if (action == null) {
				return DataResult.error(() -> "Unknown action");
			} else {
				return !action.isAllowedFromServer() ? DataResult.error(() -> "Action not allowed: " + action) : DataResult.success(action, Lifecycle.stable());
			}
		}
	}

	public static class EntityTooltipInfo {
		public static final Codec<HoverEvent.EntityTooltipInfo> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(entityTooltipInfo -> entityTooltipInfo.type),
						UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter(entityTooltipInfo -> entityTooltipInfo.id),
						ExtraCodecs.strictOptionalField(ComponentSerialization.CODEC, "name").forGetter(entityTooltipInfo -> entityTooltipInfo.name)
					)
					.apply(instance, HoverEvent.EntityTooltipInfo::new)
		);
		public final EntityType<?> type;
		public final UUID id;
		public final Optional<Component> name;
		@Nullable
		private List<Component> linesCache;

		public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, @Nullable Component component) {
			this(entityType, uUID, Optional.ofNullable(component));
		}

		public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, Optional<Component> optional) {
			this.type = entityType;
			this.id = uUID;
			this.name = optional;
		}

		public static DataResult<HoverEvent.EntityTooltipInfo> legacyCreate(Component component) {
			try {
				CompoundTag compoundTag = TagParser.parseTag(component.getString());
				Component component2 = Component.Serializer.fromJson(compoundTag.getString("name"));
				EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(compoundTag.getString("type")));
				UUID uUID = UUID.fromString(compoundTag.getString("id"));
				return DataResult.success(new HoverEvent.EntityTooltipInfo(entityType, uUID, component2));
			} catch (Exception var5) {
				return DataResult.error(() -> "Failed to parse tooltip: " + var5.getMessage());
			}
		}

		public List<Component> getTooltipLines() {
			if (this.linesCache == null) {
				this.linesCache = new ArrayList();
				this.name.ifPresent(this.linesCache::add);
				this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
				this.linesCache.add(Component.literal(this.id.toString()));
			}

			return this.linesCache;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				HoverEvent.EntityTooltipInfo entityTooltipInfo = (HoverEvent.EntityTooltipInfo)object;
				return this.type.equals(entityTooltipInfo.type) && this.id.equals(entityTooltipInfo.id) && this.name.equals(entityTooltipInfo.name);
			} else {
				return false;
			}
		}

		public int hashCode() {
			int i = this.type.hashCode();
			i = 31 * i + this.id.hashCode();
			return 31 * i + this.name.hashCode();
		}
	}

	public static class ItemStackInfo {
		public static final Codec<HoverEvent.ItemStackInfo> FULL_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(itemStackInfo -> itemStackInfo.item),
						ExtraCodecs.strictOptionalField(Codec.INT, "count", 1).forGetter(itemStackInfo -> itemStackInfo.count),
						ExtraCodecs.strictOptionalField(TagParser.AS_CODEC, "tag").forGetter(itemStackInfo -> itemStackInfo.tag)
					)
					.apply(instance, HoverEvent.ItemStackInfo::new)
		);
		public static final Codec<HoverEvent.ItemStackInfo> CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), FULL_CODEC)
			.xmap(either -> either.map(item -> new HoverEvent.ItemStackInfo(item, 1, Optional.empty()), itemStackInfo -> itemStackInfo), Either::right);
		private final Item item;
		private final int count;
		private final Optional<CompoundTag> tag;
		@Nullable
		private ItemStack itemStack;

		ItemStackInfo(Item item, int i, @Nullable CompoundTag compoundTag) {
			this(item, i, Optional.ofNullable(compoundTag));
		}

		ItemStackInfo(Item item, int i, Optional<CompoundTag> optional) {
			this.item = item;
			this.count = i;
			this.tag = optional;
		}

		public ItemStackInfo(ItemStack itemStack) {
			this(itemStack.getItem(), itemStack.getCount(), itemStack.getTag() != null ? Optional.of(itemStack.getTag().copy()) : Optional.empty());
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				HoverEvent.ItemStackInfo itemStackInfo = (HoverEvent.ItemStackInfo)object;
				return this.count == itemStackInfo.count && this.item.equals(itemStackInfo.item) && this.tag.equals(itemStackInfo.tag);
			} else {
				return false;
			}
		}

		public int hashCode() {
			int i = this.item.hashCode();
			i = 31 * i + this.count;
			return 31 * i + this.tag.hashCode();
		}

		public ItemStack getItemStack() {
			if (this.itemStack == null) {
				this.itemStack = new ItemStack(this.item, this.count);
				this.tag.ifPresent(this.itemStack::setTag);
			}

			return this.itemStack;
		}

		private static DataResult<HoverEvent.ItemStackInfo> legacyCreate(Component component) {
			try {
				CompoundTag compoundTag = TagParser.parseTag(component.getString());
				return DataResult.success(new HoverEvent.ItemStackInfo(ItemStack.of(compoundTag)));
			} catch (CommandSyntaxException var2) {
				return DataResult.error(() -> "Failed to parse item tag: " + var2.getMessage());
			}
		}
	}

	static record TypedHoverEvent<T>(HoverEvent.Action<T> action, T value) {
		public static final MapCodec<HoverEvent.TypedHoverEvent<?>> CODEC = HoverEvent.Action.CODEC
			.dispatchMap("action", HoverEvent.TypedHoverEvent::action, action -> action.codec);
		public static final MapCodec<HoverEvent.TypedHoverEvent<?>> LEGACY_CODEC = HoverEvent.Action.CODEC
			.dispatchMap("action", HoverEvent.TypedHoverEvent::action, action -> action.legacyCodec);
	}
}
