/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface CrossbowAttackMob {
    public void setChargingCrossbow(boolean var1);

    public void shootProjectile(LivingEntity var1, ItemStack var2, Projectile var3, float var4);

    @Nullable
    public LivingEntity getTarget();
}

