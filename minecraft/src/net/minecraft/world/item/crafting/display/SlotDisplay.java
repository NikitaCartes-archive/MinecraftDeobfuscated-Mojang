package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import org.apache.commons.lang3.mutable.MutableObject;

public interface SlotDisplay {
	Codec<SlotDisplay> CODEC = BuiltInRegistries.SLOT_DISPLAY.byNameCodec().dispatch(SlotDisplay::type, SlotDisplay.Type::codec);
	StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = ByteBufCodecs.registry(Registries.SLOT_DISPLAY)
		.dispatch(SlotDisplay::type, SlotDisplay.Type::streamCodec);

	void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput);

	SlotDisplay.Type<? extends SlotDisplay> type();

	default boolean isEnabled(FeatureFlagSet featureFlagSet) {
		return true;
	}

	default void resolveForStacks(SlotDisplay.ResolutionContext resolutionContext, Consumer<ItemStack> consumer) {
		this.resolve(resolutionContext, new SlotDisplay.ResolutionOutput() {
			@Override
			public void accept(Holder<Item> holder) {
				consumer.accept(new ItemStack(holder));
			}

			@Override
			public void accept(Item item) {
				consumer.accept(new ItemStack(item));
			}

			@Override
			public void accept(ItemStack itemStack) {
				consumer.accept(itemStack);
			}
		});
	}

	default List<ItemStack> resolveForStacks(SlotDisplay.ResolutionContext resolutionContext) {
		List<ItemStack> list = new ArrayList();
		this.resolveForStacks(resolutionContext, list::add);
		return list;
	}

	default ItemStack resolveForFirstStack(SlotDisplay.ResolutionContext resolutionContext) {
		MutableObject<ItemStack> mutableObject = new MutableObject<>(ItemStack.EMPTY);
		this.resolveForStacks(resolutionContext, itemStack -> {
			if (!itemStack.isEmpty() && mutableObject.getValue().isEmpty()) {
				mutableObject.setValue(itemStack);
			}
		});
		return mutableObject.getValue();
	}

	public static class AnyFuel implements SlotDisplay {
		public static final SlotDisplay.AnyFuel INSTANCE = new SlotDisplay.AnyFuel();
		public static final MapCodec<SlotDisplay.AnyFuel> MAP_CODEC = MapCodec.unit(INSTANCE);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.AnyFuel> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		public static final SlotDisplay.Type<SlotDisplay.AnyFuel> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		private AnyFuel() {
		}

		@Override
		public SlotDisplay.Type<SlotDisplay.AnyFuel> type() {
			return TYPE;
		}

		public String toString() {
			return "<any fuel>";
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
			resolutionContext.fuelValues().fuelItems().forEach(resolutionOutput::accept);
		}
	}

	public static record Composite(List<SlotDisplay> contents) implements SlotDisplay {
		public static final MapCodec<SlotDisplay.Composite> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(SlotDisplay.CODEC.listOf().fieldOf("contents").forGetter(SlotDisplay.Composite::contents))
					.apply(instance, SlotDisplay.Composite::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.Composite> STREAM_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), SlotDisplay.Composite::contents, SlotDisplay.Composite::new
		);
		public static final SlotDisplay.Type<SlotDisplay.Composite> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public SlotDisplay.Type<SlotDisplay.Composite> type() {
			return TYPE;
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
			this.contents.forEach(slotDisplay -> slotDisplay.resolve(resolutionContext, resolutionOutput));
		}

		@Override
		public boolean isEnabled(FeatureFlagSet featureFlagSet) {
			return this.contents.stream().allMatch(slotDisplay -> slotDisplay.isEnabled(featureFlagSet));
		}
	}

	public static class Empty implements SlotDisplay {
		public static final SlotDisplay.Empty INSTANCE = new SlotDisplay.Empty();
		public static final MapCodec<SlotDisplay.Empty> MAP_CODEC = MapCodec.unit(INSTANCE);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.Empty> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		public static final SlotDisplay.Type<SlotDisplay.Empty> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		private Empty() {
		}

		@Override
		public SlotDisplay.Type<SlotDisplay.Empty> type() {
			return TYPE;
		}

		public String toString() {
			return "<empty>";
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
		}
	}

	public static record ItemSlotDisplay(Holder<Item> item) implements SlotDisplay {
		public static final MapCodec<SlotDisplay.ItemSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(RegistryFixedCodec.create(Registries.ITEM).fieldOf("item").forGetter(SlotDisplay.ItemSlotDisplay::item))
					.apply(instance, SlotDisplay.ItemSlotDisplay::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.ItemSlotDisplay> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.ITEM), SlotDisplay.ItemSlotDisplay::item, SlotDisplay.ItemSlotDisplay::new
		);
		public static final SlotDisplay.Type<SlotDisplay.ItemSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		public ItemSlotDisplay(Item item) {
			this(item.builtInRegistryHolder());
		}

		@Override
		public SlotDisplay.Type<SlotDisplay.ItemSlotDisplay> type() {
			return TYPE;
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
			resolutionOutput.accept(this.item);
		}

		@Override
		public boolean isEnabled(FeatureFlagSet featureFlagSet) {
			return this.item.value().isEnabled(featureFlagSet);
		}
	}

	public static record ItemStackSlotDisplay(ItemStack stack) implements SlotDisplay {
		public static final MapCodec<SlotDisplay.ItemStackSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(ItemStack.STRICT_CODEC.fieldOf("item").forGetter(SlotDisplay.ItemStackSlotDisplay::stack))
					.apply(instance, SlotDisplay.ItemStackSlotDisplay::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.ItemStackSlotDisplay> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, SlotDisplay.ItemStackSlotDisplay::stack, SlotDisplay.ItemStackSlotDisplay::new
		);
		public static final SlotDisplay.Type<SlotDisplay.ItemStackSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public SlotDisplay.Type<SlotDisplay.ItemStackSlotDisplay> type() {
			return TYPE;
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
			resolutionOutput.accept(this.stack);
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				if (object instanceof SlotDisplay.ItemStackSlotDisplay itemStackSlotDisplay && ItemStack.matches(this.stack, itemStackSlotDisplay.stack)) {
					return true;
				}

				return false;
			}
		}

		@Override
		public boolean isEnabled(FeatureFlagSet featureFlagSet) {
			return this.stack.getItem().isEnabled(featureFlagSet);
		}
	}

	public interface ResolutionContext {
		FuelValues fuelValues();

		HolderLookup.Provider registries();

		static SlotDisplay.ResolutionContext forLevel(Level level) {
			return new SlotDisplay.ResolutionContext() {
				@Override
				public FuelValues fuelValues() {
					return level.fuelValues();
				}

				@Override
				public HolderLookup.Provider registries() {
					return level.registryAccess();
				}
			};
		}
	}

	public interface ResolutionOutput {
		void accept(Holder<Item> holder);

		void accept(Item item);

		void accept(ItemStack itemStack);
	}

	public static class SmithingTrimDemoSlotDisplay implements SlotDisplay {
		public static final SlotDisplay.SmithingTrimDemoSlotDisplay INSTANCE = new SlotDisplay.SmithingTrimDemoSlotDisplay();
		public static final MapCodec<SlotDisplay.SmithingTrimDemoSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.SmithingTrimDemoSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		public static final SlotDisplay.Type<SlotDisplay.SmithingTrimDemoSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		private SmithingTrimDemoSlotDisplay() {
		}

		@Override
		public SlotDisplay.Type<SlotDisplay.SmithingTrimDemoSlotDisplay> type() {
			return TYPE;
		}

		public String toString() {
			return "<smithing trim demo>";
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
			Optional<Holder.Reference<TrimPattern>> optional = resolutionContext.registries().lookupOrThrow(Registries.TRIM_PATTERN).listElements().findFirst();
			Optional<Holder.Reference<TrimMaterial>> optional2 = resolutionContext.registries().lookupOrThrow(Registries.TRIM_MATERIAL).get(TrimMaterials.REDSTONE);
			if (optional.isPresent() && optional2.isPresent()) {
				ItemStack itemStack = new ItemStack(Items.IRON_CHESTPLATE);
				itemStack.set(DataComponents.TRIM, new ArmorTrim((Holder<TrimMaterial>)optional2.get(), (Holder<TrimPattern>)optional.get()));
				resolutionOutput.accept(itemStack);
			}
		}
	}

	public static record TagSlotDisplay(TagKey<Item> tag) implements SlotDisplay {
		public static final MapCodec<SlotDisplay.TagSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(SlotDisplay.TagSlotDisplay::tag))
					.apply(instance, SlotDisplay.TagSlotDisplay::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.TagSlotDisplay> STREAM_CODEC = StreamCodec.composite(
			TagKey.streamCodec(Registries.ITEM), SlotDisplay.TagSlotDisplay::tag, SlotDisplay.TagSlotDisplay::new
		);
		public static final SlotDisplay.Type<SlotDisplay.TagSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public SlotDisplay.Type<SlotDisplay.TagSlotDisplay> type() {
			return TYPE;
		}

		@Override
		public void resolve(SlotDisplay.ResolutionContext resolutionContext, SlotDisplay.ResolutionOutput resolutionOutput) {
			resolutionContext.registries().lookupOrThrow(Registries.ITEM).get(this.tag).ifPresent(named -> named.forEach(resolutionOutput::accept));
		}
	}

	public static record Type<T extends SlotDisplay>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
	}
}
