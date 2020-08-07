package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SweepingEdgeEnchantment extends Enchantment {
	public SweepingEdgeEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.WEAPON, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 5 + (i - 1) * 9;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 15;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	public static float getSweepingDamageRatio(int i) {
		return 0.5F - 0.5F / (float)(i + 1);
	}
}
