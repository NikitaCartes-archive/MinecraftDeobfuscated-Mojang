/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface SimpleWaterloggedBlock
extends BucketPickup,
LiquidBlockContainer {
    @Override
    default public boolean canPlaceLiquid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return blockState.getValue(BlockStateProperties.WATERLOGGED) == false && fluid == Fluids.WATER;
    }

    @Override
    default public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!blockState.getValue(BlockStateProperties.WATERLOGGED).booleanValue() && fluidState.getType() == Fluids.WATER) {
            if (!levelAccessor.isClientSide()) {
                levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, true), 3);
                levelAccessor.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
            }
            return true;
        }
        return false;
    }

    @Override
    default public Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
            levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            return Fluids.WATER;
        }
        return Fluids.EMPTY;
    }
}

