/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ArrowFireEnchantment
extends Enchantment {
    public ArrowFireEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
        super(rarity, EnchantmentCategory.BOW, equipmentSlots);
    }

    @Override
    public int getMinCost(int i) {
        return 20;
    }

    @Override
    public int getMaxCost(int i) {
        return 50;
    }
}

