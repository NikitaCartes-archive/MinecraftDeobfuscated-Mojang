package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ItemAttributeModifiersPredicate(
	Optional<CollectionPredicate<ItemAttributeModifiers.Entry, ItemAttributeModifiersPredicate.EntryPredicate>> modifiers
) implements SingleComponentItemPredicate<ItemAttributeModifiers> {
	public static final Codec<ItemAttributeModifiersPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CollectionPredicate.codec(ItemAttributeModifiersPredicate.EntryPredicate.CODEC)
						.optionalFieldOf("modifiers")
						.forGetter(ItemAttributeModifiersPredicate::modifiers)
				)
				.apply(instance, ItemAttributeModifiersPredicate::new)
	);

	@Override
	public DataComponentType<ItemAttributeModifiers> componentType() {
		return DataComponents.ATTRIBUTE_MODIFIERS;
	}

	public boolean matches(ItemStack itemStack, ItemAttributeModifiers itemAttributeModifiers) {
		return !this.modifiers.isPresent() || ((CollectionPredicate)this.modifiers.get()).test((Iterable)itemAttributeModifiers.modifiers());
	}

	public static record EntryPredicate(
		Optional<HolderSet<Attribute>> attribute,
		Optional<ResourceLocation> id,
		MinMaxBounds.Doubles amount,
		Optional<AttributeModifier.Operation> operation,
		Optional<EquipmentSlotGroup> slot
	) implements Predicate<ItemAttributeModifiers.Entry> {
		public static final Codec<ItemAttributeModifiersPredicate.EntryPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RegistryCodecs.homogeneousList(Registries.ATTRIBUTE).optionalFieldOf("attribute").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::attribute),
						ResourceLocation.CODEC.optionalFieldOf("id").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::id),
						MinMaxBounds.Doubles.CODEC.optionalFieldOf("amount", MinMaxBounds.Doubles.ANY).forGetter(ItemAttributeModifiersPredicate.EntryPredicate::amount),
						AttributeModifier.Operation.CODEC.optionalFieldOf("operation").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::operation),
						EquipmentSlotGroup.CODEC.optionalFieldOf("slot").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::slot)
					)
					.apply(instance, ItemAttributeModifiersPredicate.EntryPredicate::new)
		);

		public boolean test(ItemAttributeModifiers.Entry entry) {
			if (this.attribute.isPresent() && !((HolderSet)this.attribute.get()).contains(entry.attribute())) {
				return false;
			} else if (this.id.isPresent() && !((ResourceLocation)this.id.get()).equals(entry.modifier().id())) {
				return false;
			} else if (!this.amount.matches(entry.modifier().amount())) {
				return false;
			} else {
				return this.operation.isPresent() && this.operation.get() != entry.modifier().operation()
					? false
					: !this.slot.isPresent() || this.slot.get() == entry.slot();
			}
		}
	}
}
