package net.minecraft.world.item.equipment;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmorMaterial(
	int durability,
	Map<ArmorType, Integer> defense,
	int enchantmentValue,
	Holder<SoundEvent> equipSound,
	float toughness,
	float knockbackResistance,
	TagKey<Item> repairIngredient,
	ResourceLocation modelId
) {
	public Item.Properties humanoidProperties(Item.Properties properties, ArmorType armorType) {
		return properties.durability(armorType.getDurability(this.durability))
			.attributes(this.createAttributes(armorType))
			.enchantable(this.enchantmentValue)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot()).setEquipSound(this.equipSound).setModel(this.modelId).build())
			.repairable(this.repairIngredient);
	}

	public Item.Properties animalProperties(Item.Properties properties, HolderSet<EntityType<?>> holderSet) {
		return properties.durability(ArmorType.BODY.getDurability(this.durability))
			.attributes(this.createAttributes(ArmorType.BODY))
			.repairable(this.repairIngredient)
			.component(
				DataComponents.EQUIPPABLE,
				Equippable.builder(EquipmentSlot.BODY).setEquipSound(this.equipSound).setModel(this.modelId).setAllowedEntities(holderSet).build()
			);
	}

	public Item.Properties animalProperties(Item.Properties properties, SoundEvent soundEvent, HolderSet<EntityType<?>> holderSet) {
		return properties.durability(ArmorType.BODY.getDurability(this.durability))
			.attributes(this.createAttributes(ArmorType.BODY))
			.repairable(this.repairIngredient)
			.component(
				DataComponents.EQUIPPABLE,
				Equippable.builder(EquipmentSlot.BODY).setEquipSound(Holder.direct(soundEvent)).setModel(this.modelId).setAllowedEntities(holderSet).build()
			);
	}

	private ItemAttributeModifiers createAttributes(ArmorType armorType) {
		int i = (Integer)this.defense.getOrDefault(armorType, 0);
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(armorType.getSlot());
		ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("armor." + armorType.getName());
		builder.add(Attributes.ARMOR, new AttributeModifier(resourceLocation, (double)i, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		builder.add(
			Attributes.ARMOR_TOUGHNESS, new AttributeModifier(resourceLocation, (double)this.toughness, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup
		);
		if (this.knockbackResistance > 0.0F) {
			builder.add(
				Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(resourceLocation, (double)this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE),
				equipmentSlotGroup
			);
		}

		return builder.build();
	}
}
