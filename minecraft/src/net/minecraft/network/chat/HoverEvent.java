package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HoverEvent {
	private static final Logger LOGGER = LogManager.getLogger();
	private final HoverEvent.Action<?> action;
	private final Object value;

	public <T> HoverEvent(HoverEvent.Action<T> action, T object) {
		this.action = action;
		this.value = object;
	}

	public HoverEvent.Action<?> getAction() {
		return this.action;
	}

	@Nullable
	public <T> T getValue(HoverEvent.Action<T> action) {
		return this.action == action ? action.cast(this.value) : null;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			HoverEvent hoverEvent = (HoverEvent)object;
			return this.action == hoverEvent.action && Objects.equals(this.value, hoverEvent.value);
		} else {
			return false;
		}
	}

	public String toString() {
		return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
	}

	public int hashCode() {
		int i = this.action.hashCode();
		return 31 * i + (this.value != null ? this.value.hashCode() : 0);
	}

	@Nullable
	public static HoverEvent deserialize(JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "action", null);
		if (string == null) {
			return null;
		} else {
			HoverEvent.Action<?> action = HoverEvent.Action.getByName(string);
			if (action == null) {
				return null;
			} else {
				JsonElement jsonElement = jsonObject.get("contents");
				if (jsonElement != null) {
					return action.deserialize(jsonElement);
				} else {
					Component component = Component.Serializer.fromJson(jsonObject.get("value"));
					return component != null ? action.deserializeFromLegacy(component) : null;
				}
			}
		}
	}

	public JsonObject serialize() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("action", this.action.getName());
		jsonObject.add("contents", this.action.serializeArg(this.value));
		return jsonObject;
	}

	public static class Action<T> {
		public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>(
			"show_text", true, Component.Serializer::fromJson, Component.Serializer::toJsonTree, Function.identity()
		);
		public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>(
			"show_item",
			true,
			jsonElement -> HoverEvent.ItemStackInfo.create(jsonElement),
			object -> ((HoverEvent.ItemStackInfo)object).serialize(),
			component -> HoverEvent.ItemStackInfo.create(component)
		);
		public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>(
			"show_entity", true, HoverEvent.EntityTooltipInfo::create, HoverEvent.EntityTooltipInfo::serialize, HoverEvent.EntityTooltipInfo::create
		);
		private static final Map<String, HoverEvent.Action> LOOKUP = (Map<String, HoverEvent.Action>)Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY)
			.collect(ImmutableMap.toImmutableMap(HoverEvent.Action::getName, action -> action));
		private final String name;
		private final boolean allowFromServer;
		private final Function<JsonElement, T> argDeserializer;
		private final Function<T, JsonElement> argSerializer;
		private final Function<Component, T> legacyArgDeserializer;

		public Action(String string, boolean bl, Function<JsonElement, T> function, Function<T, JsonElement> function2, Function<Component, T> function3) {
			this.name = string;
			this.allowFromServer = bl;
			this.argDeserializer = function;
			this.argSerializer = function2;
			this.legacyArgDeserializer = function3;
		}

		public boolean isAllowedFromServer() {
			return this.allowFromServer;
		}

		public String getName() {
			return this.name;
		}

		@Nullable
		public static HoverEvent.Action getByName(String string) {
			return (HoverEvent.Action)LOOKUP.get(string);
		}

		private T cast(Object object) {
			return (T)object;
		}

		public HoverEvent deserialize(JsonElement jsonElement) {
			T object = (T)this.argDeserializer.apply(jsonElement);
			return new HoverEvent(this, object);
		}

		public HoverEvent deserializeFromLegacy(Component component) {
			T object = (T)this.legacyArgDeserializer.apply(component);
			return new HoverEvent(this, object);
		}

		public JsonElement serializeArg(Object object) {
			return (JsonElement)this.argSerializer.apply(this.cast(object));
		}

		public String toString() {
			return "<action " + this.name + ">";
		}
	}

	public static class EntityTooltipInfo {
		public final EntityType<?> type;
		public final UUID id;
		@Nullable
		public final Component name;
		@Nullable
		@Environment(EnvType.CLIENT)
		private List<Component> linesCache;

		public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, @Nullable Component component) {
			this.type = entityType;
			this.id = uUID;
			this.name = component;
		}

		@Nullable
		public static HoverEvent.EntityTooltipInfo create(JsonElement jsonElement) {
			if (!jsonElement.isJsonObject()) {
				return null;
			} else {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "type")));
				UUID uUID = UUID.fromString(GsonHelper.getAsString(jsonObject, "id"));
				Component component = Component.Serializer.fromJson(jsonObject.get("name"));
				return new HoverEvent.EntityTooltipInfo(entityType, uUID, component);
			}
		}

		@Nullable
		public static HoverEvent.EntityTooltipInfo create(Component component) {
			try {
				CompoundTag compoundTag = TagParser.parseTag(component.getString());
				Component component2 = Component.Serializer.fromJson(compoundTag.getString("name"));
				EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(compoundTag.getString("type")));
				UUID uUID = UUID.fromString(compoundTag.getString("id"));
				return new HoverEvent.EntityTooltipInfo(entityType, uUID, component2);
			} catch (CommandSyntaxException | JsonSyntaxException var5) {
				return null;
			}
		}

		public JsonElement serialize() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("type", Registry.ENTITY_TYPE.getKey(this.type).toString());
			jsonObject.addProperty("id", this.id.toString());
			if (this.name != null) {
				jsonObject.add("name", Component.Serializer.toJsonTree(this.name));
			}

			return jsonObject;
		}

		@Environment(EnvType.CLIENT)
		public List<Component> getTooltipLines() {
			if (this.linesCache == null) {
				this.linesCache = Lists.<Component>newArrayList();
				if (this.name != null) {
					this.linesCache.add(this.name);
				}

				this.linesCache.add(new TranslatableComponent("gui.entity_tooltip.type", this.type.getDescription()));
				this.linesCache.add(new TextComponent(this.id.toString()));
			}

			return this.linesCache;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				HoverEvent.EntityTooltipInfo entityTooltipInfo = (HoverEvent.EntityTooltipInfo)object;
				return this.type.equals(entityTooltipInfo.type) && this.id.equals(entityTooltipInfo.id) && Objects.equals(this.name, entityTooltipInfo.name);
			} else {
				return false;
			}
		}

		public int hashCode() {
			int i = this.type.hashCode();
			i = 31 * i + this.id.hashCode();
			return 31 * i + (this.name != null ? this.name.hashCode() : 0);
		}
	}

	public static class ItemStackInfo {
		private final Item item;
		private final int count;
		@Nullable
		private final CompoundTag tag;
		@Nullable
		@Environment(EnvType.CLIENT)
		private ItemStack itemStack;

		ItemStackInfo(Item item, int i, @Nullable CompoundTag compoundTag) {
			this.item = item;
			this.count = i;
			this.tag = compoundTag;
		}

		public ItemStackInfo(ItemStack itemStack) {
			this(itemStack.getItem(), itemStack.getCount(), itemStack.getTag() != null ? itemStack.getTag().copy() : null);
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				HoverEvent.ItemStackInfo itemStackInfo = (HoverEvent.ItemStackInfo)object;
				return this.count == itemStackInfo.count && this.item.equals(itemStackInfo.item) && Objects.equals(this.tag, itemStackInfo.tag);
			} else {
				return false;
			}
		}

		public int hashCode() {
			int i = this.item.hashCode();
			i = 31 * i + this.count;
			return 31 * i + (this.tag != null ? this.tag.hashCode() : 0);
		}

		@Environment(EnvType.CLIENT)
		public ItemStack getItemStack() {
			if (this.itemStack == null) {
				this.itemStack = new ItemStack(this.item, this.count);
				if (this.tag != null) {
					this.itemStack.setTag(this.tag);
				}
			}

			return this.itemStack;
		}

		private static HoverEvent.ItemStackInfo create(JsonElement jsonElement) {
			if (jsonElement.isJsonPrimitive()) {
				return new HoverEvent.ItemStackInfo(Registry.ITEM.get(new ResourceLocation(jsonElement.getAsString())), 1, null);
			} else {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "item");
				Item item = Registry.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "id")));
				int i = GsonHelper.getAsInt(jsonObject, "count", 1);
				if (jsonObject.has("tag")) {
					String string = GsonHelper.getAsString(jsonObject, "tag");

					try {
						CompoundTag compoundTag = TagParser.parseTag(string);
						return new HoverEvent.ItemStackInfo(item, i, compoundTag);
					} catch (CommandSyntaxException var6) {
						HoverEvent.LOGGER.warn("Failed to parse tag: {}", string, var6);
					}
				}

				return new HoverEvent.ItemStackInfo(item, i, null);
			}
		}

		@Nullable
		private static HoverEvent.ItemStackInfo create(Component component) {
			try {
				CompoundTag compoundTag = TagParser.parseTag(component.getString());
				return new HoverEvent.ItemStackInfo(ItemStack.of(compoundTag));
			} catch (CommandSyntaxException var2) {
				HoverEvent.LOGGER.warn("Failed to parse item tag: {}", component, var2);
				return null;
			}
		}

		private JsonElement serialize() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("id", Registry.ITEM.getKey(this.item).toString());
			if (this.count != 1) {
				jsonObject.addProperty("count", this.count);
			}

			if (this.tag != null) {
				jsonObject.addProperty("tag", this.tag.toString());
			}

			return jsonObject;
		}
	}
}
