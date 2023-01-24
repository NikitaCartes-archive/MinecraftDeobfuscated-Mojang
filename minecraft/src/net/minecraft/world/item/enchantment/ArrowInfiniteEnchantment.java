package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowInfiniteEnchantment extends Enchantment {
	public ArrowInfiniteEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.BOW, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 20;
	}

	@Override
	public int getMaxCost(int i) {
		return 50;
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return enchantment instanceof MendingEnchantment ? false : super.checkCompatibility(enchantment);
	}
}
