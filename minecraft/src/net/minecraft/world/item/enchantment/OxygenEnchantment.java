package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class OxygenEnchantment extends Enchantment {
	public OxygenEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.ARMOR_HEAD, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 10 * i;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 30;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}
