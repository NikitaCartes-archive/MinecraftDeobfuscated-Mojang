/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                levelAccessor.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
            }
            return true;
        }
        return false;
    }

    @Override
    default public ItemStack pickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
            levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            if (!blockState.canSurvive(levelAccessor, blockPos)) {
                levelAccessor.destroyBlock(blockPos, true);
            }
            return new ItemStack(Items.WATER_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    @Override
    default public Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }
}

