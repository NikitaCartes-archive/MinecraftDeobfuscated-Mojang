package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowFireEnchantment extends Enchantment {
	public ArrowFireEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
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
}
