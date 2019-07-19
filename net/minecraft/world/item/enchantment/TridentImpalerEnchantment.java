/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class TridentImpalerEnchantment
extends Enchantment {
    public TridentImpalerEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
        super(rarity, EnchantmentCategory.TRIDENT, equipmentSlots);
    }

    @Override
    public int getMinCost(int i) {
        return 1 + (i - 1) * 8;
    }

    @Override
    public int getMaxCost(int i) {
        return this.getMinCost(i) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public float getDamageBonus(int i, MobType mobType) {
        if (mobType == MobType.WATER) {
            return (float)i * 2.5f;
        }
        return 0.0f;
    }
}

