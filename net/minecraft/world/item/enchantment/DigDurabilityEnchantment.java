/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import java.util.Random;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class DigDurabilityEnchantment
extends Enchantment {
    protected DigDurabilityEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
        super(rarity, EnchantmentCategory.BREAKABLE, equipmentSlots);
    }

    @Override
    public int getMinCost(int i) {
        return 5 + (i - 1) * 8;
    }

    @Override
    public int getMaxCost(int i) {
        return super.getMinCost(i) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        if (itemStack.isDamageableItem()) {
            return true;
        }
        return super.canEnchant(itemStack);
    }

    public static boolean shouldIgnoreDurabilityDrop(ItemStack itemStack, int i, Random random) {
        if (itemStack.getItem() instanceof ArmorItem && random.nextFloat() < 0.6f) {
            return false;
        }
        return random.nextInt(i + 1) > 0;
    }
}

