package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ItemAttributeModifiers(List<ItemAttributeModifiers.Entry> modifiers, boolean showInTooltip) {
	public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of(), true);
	private static final Codec<ItemAttributeModifiers> FULL_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemAttributeModifiers.Entry.CODEC.listOf().fieldOf("modifiers").forGetter(ItemAttributeModifiers::modifiers),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "show_in_tooltip", true).forGetter(ItemAttributeModifiers::showInTooltip)
				)
				.apply(instance, ItemAttributeModifiers::new)
	);
	public static final Codec<ItemAttributeModifiers> CODEC = ExtraCodecs.withAlternative(
		FULL_CODEC, ItemAttributeModifiers.Entry.CODEC.listOf(), list -> new ItemAttributeModifiers(list, true)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(
		ItemAttributeModifiers.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
		ItemAttributeModifiers::modifiers,
		ByteBufCodecs.BOOL,
		ItemAttributeModifiers::showInTooltip,
		ItemAttributeModifiers::new
	);
	public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(
		new DecimalFormat("#.##"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
	);

	public ItemAttributeModifiers withTooltip(boolean bl) {
		return new ItemAttributeModifiers(this.modifiers, bl);
	}

	public static ItemAttributeModifiers.Builder builder() {
		return new ItemAttributeModifiers.Builder();
	}

	public ItemAttributeModifiers withModifierAdded(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
		return new ItemAttributeModifiers(
			Util.copyAndAdd(this.modifiers, new ItemAttributeModifiers.Entry(holder, attributeModifier, equipmentSlotGroup)), this.showInTooltip
		);
	}

	public void forEach(EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
		for (ItemAttributeModifiers.Entry entry : this.modifiers) {
			if (entry.slot.test(equipmentSlot)) {
				biConsumer.accept(entry.attribute, entry.modifier);
			}
		}
	}

	public double compute(double d, EquipmentSlot equipmentSlot) {
		double e = d;

		for (ItemAttributeModifiers.Entry entry : this.modifiers) {
			if (entry.slot.test(equipmentSlot)) {
				double f = entry.modifier.amount();

				e += switch (entry.modifier.operation()) {
					case ADD_VALUE -> f;
					case ADD_MULTIPLIED_BASE -> f * d;
					case ADD_MULTIPLIED_TOTAL -> f * e;
				};
			}
		}

		return e;
	}

	public static class Builder {
		private final ImmutableList.Builder<ItemAttributeModifiers.Entry> entries = ImmutableList.builder();

		Builder() {
		}

		public ItemAttributeModifiers.Builder add(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
			this.entries.add(new ItemAttributeModifiers.Entry(holder, attributeModifier, equipmentSlotGroup));
			return this;
		}

		public ItemAttributeModifiers build() {
			return new ItemAttributeModifiers(this.entries.build(), true);
		}
	}

	public static record Entry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
		public static final Codec<ItemAttributeModifiers.Entry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("type").forGetter(ItemAttributeModifiers.Entry::attribute),
						AttributeModifier.MAP_CODEC.forGetter(ItemAttributeModifiers.Entry::modifier),
						ExtraCodecs.strictOptionalField(EquipmentSlotGroup.CODEC, "slot", EquipmentSlotGroup.ANY).forGetter(ItemAttributeModifiers.Entry::slot)
					)
					.apply(instance, ItemAttributeModifiers.Entry::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Entry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
			ItemAttributeModifiers.Entry::attribute,
			AttributeModifier.STREAM_CODEC,
			ItemAttributeModifiers.Entry::modifier,
			EquipmentSlotGroup.STREAM_CODEC,
			ItemAttributeModifiers.Entry::slot,
			ItemAttributeModifiers.Entry::new
		);
	}
}
