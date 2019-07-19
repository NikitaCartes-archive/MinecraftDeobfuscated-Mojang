package net.minecraft.world.item;

import net.minecraft.world.entity.EquipmentSlot;

public class DyeableArmorItem extends ArmorItem implements DyeableLeatherItem {
	public DyeableArmorItem(ArmorMaterial armorMaterial, EquipmentSlot equipmentSlot, Item.Properties properties) {
		super(armorMaterial, equipmentSlot, properties);
	}
}
