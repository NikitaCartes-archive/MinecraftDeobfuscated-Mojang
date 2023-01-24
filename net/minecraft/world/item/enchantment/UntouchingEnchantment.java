/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class UntouchingEnchantment
extends Enchantment {
    protected UntouchingEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
        super(rarity, EnchantmentCategory.DIGGER, equipmentSlots);
    }

    @Override
    public int getMinCost(int i) {
        return 15;
    }

    @Override
    public int getMaxCost(int i) {
        return super.getMinCost(i) + 50;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.BLOCK_FORTUNE;
    }
}

