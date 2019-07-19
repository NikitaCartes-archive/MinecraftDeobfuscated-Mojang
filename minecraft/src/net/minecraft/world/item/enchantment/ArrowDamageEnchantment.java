package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowDamageEnchantment extends Enchantment {
	public ArrowDamageEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.BOW, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 1 + (i - 1) * 10;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 15;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}
}
