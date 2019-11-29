package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class ChoppingEnchantment extends Enchantment {
	public ChoppingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.AXE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 5 + (i - 1) * 20;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 20;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public float getDamageBonus(int i, LivingEntity livingEntity) {
		return (float)i;
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return !(enchantment instanceof DamageEnchantment) && super.checkCompatibility(enchantment);
	}
}
