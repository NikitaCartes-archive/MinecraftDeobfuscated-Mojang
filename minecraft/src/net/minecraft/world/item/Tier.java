package net.minecraft.world.item;

import net.minecraft.world.item.crafting.Ingredient;

public interface Tier {
	int getUses();

	float getSpeed();

	float getAttackDamageBonus();

	int getLevel();

	int getEnchantmentValue();

	Ingredient getRepairIngredient();
}
