package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class UntouchingEnchantment extends Enchantment {
	protected UntouchingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.DIGGER, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 15;
	}

	@Override
	public int getMaxCost(int i) {
		return super.getMinCost(i) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.BLOCK_FORTUNE;
	}
}
