/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDragonPhaseInstance
implements DragonPhaseInstance {
    protected final EnderDragon dragon;

    public AbstractDragonPhaseInstance(EnderDragon enderDragon) {
        this.dragon = enderDragon;
    }

    @Override
    public boolean isSitting() {
        return false;
    }

    @Override
    public void doClientTick() {
    }

    @Override
    public void doServerTick() {
    }

    @Override
    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource, @Nullable Player player) {
    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }

    @Override
    public float getFlySpeed() {
        return 0.6f;
    }

    @Override
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return null;
    }

    @Override
    public float onHurt(DamageSource damageSource, float f) {
        return f;
    }

    @Override
    public float getTurnSpeed() {
        float f = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.dragon.getDeltaMovement())) + 1.0f;
        float g = Math.min(f, 40.0f);
        return 0.7f / g / f;
    }
}

