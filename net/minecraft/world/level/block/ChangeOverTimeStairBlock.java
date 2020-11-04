/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChangeOverTimeStairBlock
extends StairBlock
implements ChangeOverTimeBlock {
    private final Block changeTo;

    public ChangeOverTimeStairBlock(BlockState blockState, BlockBehaviour.Properties properties, Block block) {
        super(blockState, properties);
        this.changeTo = block;
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        this.scheduleChange(level, this, blockPos);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        this.change(serverLevel, blockState, blockPos);
    }

    @Override
    public BlockState getChangeTo(BlockState blockState) {
        return (BlockState)((BlockState)((BlockState)((BlockState)this.changeTo.defaultBlockState().setValue(FACING, blockState.getValue(FACING))).setValue(HALF, blockState.getValue(HALF))).setValue(SHAPE, blockState.getValue(SHAPE))).setValue(WATERLOGGED, blockState.getValue(WATERLOGGED));
    }
}

