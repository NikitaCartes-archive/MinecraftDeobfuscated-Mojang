/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class QuickChargeEnchantment
extends Enchantment {
    public QuickChargeEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
        super(rarity, EnchantmentCategory.CROSSBOW, equipmentSlots);
    }

    @Override
    public int getMinCost(int i) {
        return 12 + (i - 1) * 20;
    }

    @Override
    public int getMaxCost(int i) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}

