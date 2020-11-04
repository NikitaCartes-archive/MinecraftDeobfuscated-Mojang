/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;

public interface TickingBlockEntity {
    public void tick();

    public boolean isRemoved();

    public BlockPos getPos();

    public String getType();
}

