/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.world.item.crafting.Ingredient;

public interface Tier {
    public int getUses();

    public float getSpeed();

    public float getAttackDamageBonus();

    public int getLevel();

    public int getEnchantmentValue();

    public Ingredient getRepairIngredient();
}

