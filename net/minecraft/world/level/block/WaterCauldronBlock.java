/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WaterCauldronBlock
extends AbstractCauldronBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    public WaterCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, CauldronInteraction.WATER);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LEVEL, 1));
    }

    @Override
    protected double getContentHeight(BlockState blockState) {
        return (double)(6 + blockState.getValue(LEVEL) * 3) / 16.0;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!level.isClientSide && entity.isOnFire() && this.isEntityInsideContent(blockState, blockPos, entity)) {
            entity.clearFire();
            WaterCauldronBlock.lowerWaterLevel(blockState, level, blockPos);
        }
    }

    public static void lowerWaterLevel(BlockState blockState, Level level, BlockPos blockPos) {
        int i = blockState.getValue(LEVEL) - 1;
        level.setBlockAndUpdate(blockPos, i == 0 ? Blocks.CAULDRON.defaultBlockState() : (BlockState)blockState.setValue(LEVEL, i));
    }

    @Override
    public void handleRain(BlockState blockState, Level level, BlockPos blockPos) {
        if (!CauldronBlock.shouldHandleRain(level, blockPos) || blockState.getValue(LEVEL) == 3) {
            return;
        }
        level.setBlockAndUpdate(blockPos, (BlockState)blockState.cycle(LEVEL));
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return blockState.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}

