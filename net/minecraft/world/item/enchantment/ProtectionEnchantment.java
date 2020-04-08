/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class ProtectionEnchantment
extends Enchantment {
    public final Type type;

    public ProtectionEnchantment(Enchantment.Rarity rarity, Type type, EquipmentSlot ... equipmentSlots) {
        super(rarity, type == Type.FALL ? EnchantmentCategory.ARMOR_FEET : EnchantmentCategory.ARMOR, equipmentSlots);
        this.type = type;
    }

    @Override
    public int getMinCost(int i) {
        return this.type.getMinCost() + (i - 1) * this.type.getLevelCost();
    }

    @Override
    public int getMaxCost(int i) {
        return this.getMinCost(i) + this.type.getLevelCost();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getDamageProtection(int i, DamageSource damageSource) {
        if (damageSource.isBypassInvul()) {
            return 0;
        }
        if (this.type == Type.ALL) {
            return i;
        }
        if (this.type == Type.FIRE && damageSource.isFire()) {
            return i * 2;
        }
        if (this.type == Type.FALL && damageSource == DamageSource.FALL) {
            return i * 3;
        }
        if (this.type == Type.EXPLOSION && damageSource.isExplosion()) {
            return i * 2;
        }
        if (this.type == Type.PROJECTILE && damageSource.isProjectile()) {
            return i * 2;
        }
        return 0;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        if (enchantment instanceof ProtectionEnchantment) {
            ProtectionEnchantment protectionEnchantment = (ProtectionEnchantment)enchantment;
            if (this.type == protectionEnchantment.type) {
                return false;
            }
            return this.type == Type.FALL || protectionEnchantment.type == Type.FALL;
        }
        return super.checkCompatibility(enchantment);
    }

    public static int getFireAfterDampener(LivingEntity livingEntity, int i) {
        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, livingEntity);
        if (j > 0) {
            i -= Mth.floor((float)i * ((float)j * 0.15f));
        }
        return i;
    }

    public static double getExplosionKnockbackAfterDampener(LivingEntity livingEntity, double d) {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, livingEntity);
        if (i > 0) {
            d -= (double)Mth.floor(d * (double)((float)i * 0.15f));
        }
        return d;
    }

    public static enum Type {
        ALL("all", 1, 11),
        FIRE("fire", 10, 8),
        FALL("fall", 5, 6),
        EXPLOSION("explosion", 5, 8),
        PROJECTILE("projectile", 3, 6);

        private final String name;
        private final int minCost;
        private final int levelCost;

        private Type(String string2, int j, int k) {
            this.name = string2;
            this.minCost = j;
            this.levelCost = k;
        }

        public int getMinCost() {
            return this.minCost;
        }

        public int getLevelCost() {
            return this.levelCost;
        }
    }
}

