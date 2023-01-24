/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;

public interface ArmorMaterial {
    public int getDurabilityForType(ArmorItem.Type var1);

    public int getDefenseForType(ArmorItem.Type var1);

    public int getEnchantmentValue();

    public SoundEvent getEquipSound();

    public Ingredient getRepairIngredient();

    public String getName();

    public float getToughness();

    public float getKnockbackResistance();

    public boolean canHaveTrims();
}

