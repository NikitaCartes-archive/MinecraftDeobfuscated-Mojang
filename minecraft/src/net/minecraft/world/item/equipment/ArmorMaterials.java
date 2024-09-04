package net.minecraft.world.item.equipment;

import java.util.EnumMap;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;

public interface ArmorMaterials {
	ArmorMaterial LEATHER = new ArmorMaterial(5, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 1);
		enumMap.put(ArmorType.LEGGINGS, 2);
		enumMap.put(ArmorType.CHESTPLATE, 3);
		enumMap.put(ArmorType.HELMET, 1);
		enumMap.put(ArmorType.BODY, 3);
	}), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ItemTags.REPAIRS_LEATHER_ARMOR, EquipmentModels.LEATHER);
	ArmorMaterial CHAIN = new ArmorMaterial(15, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 1);
		enumMap.put(ArmorType.LEGGINGS, 4);
		enumMap.put(ArmorType.CHESTPLATE, 5);
		enumMap.put(ArmorType.HELMET, 2);
		enumMap.put(ArmorType.BODY, 4);
	}), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, ItemTags.REPAIRS_CHAIN_ARMOR, EquipmentModels.CHAIN);
	ArmorMaterial IRON = new ArmorMaterial(15, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 2);
		enumMap.put(ArmorType.LEGGINGS, 5);
		enumMap.put(ArmorType.CHESTPLATE, 6);
		enumMap.put(ArmorType.HELMET, 2);
		enumMap.put(ArmorType.BODY, 5);
	}), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ItemTags.REPAIRS_IRON_ARMOR, EquipmentModels.IRON);
	ArmorMaterial GOLD = new ArmorMaterial(15, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 1);
		enumMap.put(ArmorType.LEGGINGS, 3);
		enumMap.put(ArmorType.CHESTPLATE, 5);
		enumMap.put(ArmorType.HELMET, 2);
		enumMap.put(ArmorType.BODY, 7);
	}), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, ItemTags.REPAIRS_GOLD_ARMOR, EquipmentModels.GOLD);
	ArmorMaterial DIAMOND = new ArmorMaterial(33, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 3);
		enumMap.put(ArmorType.LEGGINGS, 6);
		enumMap.put(ArmorType.CHESTPLATE, 8);
		enumMap.put(ArmorType.HELMET, 3);
		enumMap.put(ArmorType.BODY, 11);
	}), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, ItemTags.REPAIRS_DIAMOND_ARMOR, EquipmentModels.DIAMOND);
	ArmorMaterial TURTLE_SCUTE = new ArmorMaterial(25, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 2);
		enumMap.put(ArmorType.LEGGINGS, 5);
		enumMap.put(ArmorType.CHESTPLATE, 6);
		enumMap.put(ArmorType.HELMET, 2);
		enumMap.put(ArmorType.BODY, 5);
	}), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, ItemTags.REPAIRS_TURTLE_HELMET, EquipmentModels.TURTLE_SCUTE);
	ArmorMaterial NETHERITE = new ArmorMaterial(37, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 3);
		enumMap.put(ArmorType.LEGGINGS, 6);
		enumMap.put(ArmorType.CHESTPLATE, 8);
		enumMap.put(ArmorType.HELMET, 3);
		enumMap.put(ArmorType.BODY, 11);
	}), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, ItemTags.REPAIRS_NETHERITE_ARMOR, EquipmentModels.NETHERITE);
	ArmorMaterial ARMADILLO_SCUTE = new ArmorMaterial(4, Util.make(new EnumMap(ArmorType.class), enumMap -> {
		enumMap.put(ArmorType.BOOTS, 3);
		enumMap.put(ArmorType.LEGGINGS, 6);
		enumMap.put(ArmorType.CHESTPLATE, 8);
		enumMap.put(ArmorType.HELMET, 3);
		enumMap.put(ArmorType.BODY, 11);
	}), 10, SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, ItemTags.REPAIRS_WOLF_ARMOR, EquipmentModels.ARMADILLO_SCUTE);
}
