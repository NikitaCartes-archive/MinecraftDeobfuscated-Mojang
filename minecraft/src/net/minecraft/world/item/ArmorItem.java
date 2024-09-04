package net.minecraft.world.item;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class ArmorItem extends Item {
	public ArmorItem(ArmorMaterial armorMaterial, ArmorType armorType, Item.Properties properties) {
		super(armorMaterial.humanoidProperties(properties, armorType));
	}
}
