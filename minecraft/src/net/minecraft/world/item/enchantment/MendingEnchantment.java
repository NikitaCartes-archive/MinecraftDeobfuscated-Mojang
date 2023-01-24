package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
	public MendingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.BREAKABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return i * 25;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 50;
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}
}
