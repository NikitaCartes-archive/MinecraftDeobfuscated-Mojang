package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.Ingredient;

public interface ArmorMaterial {
	int getDurabilityForType(ArmorItem.Type type);

	int getDefenseForType(ArmorItem.Type type);

	int getEnchantmentValue();

	SoundEvent getEquipSound();

	Ingredient getRepairIngredient();

	String getName();

	float getToughness();

	float getKnockbackResistance();

	boolean canHaveTrims();
}
