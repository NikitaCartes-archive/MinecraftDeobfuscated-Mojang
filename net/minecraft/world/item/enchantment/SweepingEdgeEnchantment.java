/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SweepingEdgeEnchantment
extends Enchantment {
    public SweepingEdgeEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
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
        return 1.0f - 1.0f / (float)(i + 1);
    }
}

