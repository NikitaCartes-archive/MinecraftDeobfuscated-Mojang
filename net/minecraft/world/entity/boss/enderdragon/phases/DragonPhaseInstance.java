/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface DragonPhaseInstance {
    public boolean isSitting();

    public void doClientTick();

    public void doServerTick();

    public void onCrystalDestroyed(EndCrystal var1, BlockPos var2, DamageSource var3, @Nullable Player var4);

    public void begin();

    public void end();

    public float getFlySpeed();

    public float getTurnSpeed();

    public EnderDragonPhase<? extends DragonPhaseInstance> getPhase();

    @Nullable
    public Vec3 getFlyTargetLocation();

    public float onHurt(DamageSource var1, float var2);
}

