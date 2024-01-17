package net.minecraft.world.item.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class WaterWalkerEnchantment extends Enchantment {
	public WaterWalkerEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.FOOT_ARMOR_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return i * 10;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 15;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.FROST_WALKER;
	}
}
