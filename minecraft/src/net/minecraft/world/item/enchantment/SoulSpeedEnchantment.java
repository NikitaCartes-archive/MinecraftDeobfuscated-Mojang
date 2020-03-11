package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SoulSpeedEnchantment extends Enchantment {
	public SoulSpeedEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.ARMOR_FEET, equipmentSlots);
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
	public boolean isTreasureOnly() {
		return true;
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public boolean isDiscoverable() {
		return false;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}
