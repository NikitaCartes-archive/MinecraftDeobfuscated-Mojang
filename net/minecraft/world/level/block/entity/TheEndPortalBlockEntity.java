/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TheEndPortalBlockEntity
extends BlockEntity {
    protected TheEndPortalBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public TheEndPortalBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(BlockEntityType.END_PORTAL, blockPos, blockState);
    }

    public boolean shouldRenderFace(Direction direction) {
        return direction.getAxis() == Direction.Axis.Y;
    }
}

