package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class WaterWorkerEnchantment extends Enchantment {
	public WaterWorkerEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.ARMOR_HEAD, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 1;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 40;
	}
}
