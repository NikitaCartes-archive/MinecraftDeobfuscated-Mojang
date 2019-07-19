package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowKnockbackEnchantment extends Enchantment {
	public ArrowKnockbackEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.BOW, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 12 + (i - 1) * 20;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 25;
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}
}
