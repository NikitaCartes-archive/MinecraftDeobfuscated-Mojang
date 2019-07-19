/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface PositionWrapper {
    public BlockPos getPos();

    public Vec3 getLookAtPos();

    public boolean isVisible(LivingEntity var1);
}

