/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class TrappedChestBlockEntity
extends ChestBlockEntity {
    public TrappedChestBlockEntity() {
        super(BlockEntityType.TRAPPED_CHEST);
    }

    @Override
    protected void signalOpenCount() {
        super.signalOpenCount();
        this.level.updateNeighborsAt(this.worldPosition.below(), this.getBlockState().getBlock());
    }
}

