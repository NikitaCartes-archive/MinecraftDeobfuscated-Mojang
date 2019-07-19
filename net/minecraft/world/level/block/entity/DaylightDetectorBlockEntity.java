/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightDetectorBlockEntity
extends BlockEntity
implements TickableBlockEntity {
    public DaylightDetectorBlockEntity() {
        super(BlockEntityType.DAYLIGHT_DETECTOR);
    }

    @Override
    public void tick() {
        BlockState blockState;
        Block block;
        if (this.level != null && !this.level.isClientSide && this.level.getGameTime() % 20L == 0L && (block = (blockState = this.getBlockState()).getBlock()) instanceof DaylightDetectorBlock) {
            DaylightDetectorBlock.updateSignalStrength(blockState, this.level, this.worldPosition);
        }
    }
}

