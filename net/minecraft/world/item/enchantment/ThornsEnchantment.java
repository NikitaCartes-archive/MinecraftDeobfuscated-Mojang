/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import java.util.Map;
import java.util.Random;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class ThornsEnchantment
extends Enchantment {
    private static final float CHANCE_PER_LEVEL = 0.15f;

    public ThornsEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlots) {
        super(rarity, EnchantmentCategory.ARMOR_CHEST, equipmentSlots);
    }

    @Override
    public int getMinCost(int i) {
        return 10 + 20 * (i - 1);
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
        if (itemStack.getItem() instanceof ArmorItem) {
            return true;
        }
        return super.canEnchant(itemStack);
    }

    @Override
    public void doPostHurt(LivingEntity livingEntity2, Entity entity, int i) {
        Random random = livingEntity2.getRandom();
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, livingEntity2);
        if (ThornsEnchantment.shouldHit(i, random)) {
            if (entity != null) {
                entity.hurt(DamageSource.thorns(livingEntity2), ThornsEnchantment.getDamage(i, random));
            }
            if (entry != null) {
                entry.getValue().hurtAndBreak(2, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent((EquipmentSlot)((Object)((Object)entry.getKey()))));
            }
        }
    }

    public static boolean shouldHit(int i, Random random) {
        if (i <= 0) {
            return false;
        }
        return random.nextFloat() < 0.15f * (float)i;
    }

    public static int getDamage(int i, Random random) {
        if (i > 10) {
            return i - 10;
        }
        return 1 + random.nextInt(4);
    }
}

