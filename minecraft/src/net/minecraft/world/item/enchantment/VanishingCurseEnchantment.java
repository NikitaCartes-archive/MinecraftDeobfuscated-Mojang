package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class VanishingCurseEnchantment extends Enchantment {
	public VanishingCurseEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.VANISHABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 25;
	}

	@Override
	public int getMaxCost(int i) {
		return 50;
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}

	@Override
	public boolean isCurse() {
		return true;
	}
}
