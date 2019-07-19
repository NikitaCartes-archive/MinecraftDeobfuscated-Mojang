package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class KnockbackEnchantment extends Enchantment {
	protected KnockbackEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.WEAPON, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 5 + 20 * (i - 1);
	}

	@Override
	public int getMaxCost(int i) {
		return super.getMinCost(i) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}
}
