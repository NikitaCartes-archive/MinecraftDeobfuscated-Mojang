/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantHeadBlock
extends GrowingPlantBlock
implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    public static final int MAX_AGE = 25;
    private final double growPerTickProbability;

    protected GrowingPlantHeadBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean bl, double d) {
        super(properties, direction, voxelShape, bl);
        this.growPerTickProbability = d;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    @Override
    public BlockState getStateForPlacement(LevelAccessor levelAccessor) {
        return (BlockState)this.defaultBlockState().setValue(AGE, levelAccessor.getRandom().nextInt(25));
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(AGE) < 25;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2;
        if (blockState.getValue(AGE) < 25 && randomSource.nextDouble() < this.growPerTickProbability && this.canGrowInto(serverLevel.getBlockState(blockPos2 = blockPos.relative(this.growthDirection)))) {
            serverLevel.setBlockAndUpdate(blockPos2, this.getGrowIntoState(blockState, serverLevel.random));
        }
    }

    protected BlockState getGrowIntoState(BlockState blockState, RandomSource randomSource) {
        return (BlockState)blockState.cycle(AGE);
    }

    public BlockState getMaxAgeState(BlockState blockState) {
        return (BlockState)blockState.setValue(AGE, 25);
    }

    public boolean isMaxAge(BlockState blockState) {
        return blockState.getValue(AGE) == 25;
    }

    protected BlockState updateBodyAfterConvertedFromHead(BlockState blockState, BlockState blockState2) {
        return blockState2;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == this.growthDirection.getOpposite() && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.scheduleTick(blockPos, this, 1);
        }
        if (direction == this.growthDirection && (blockState2.is(this) || blockState2.is(this.getBodyBlock()))) {
            return this.updateBodyAfterConvertedFromHead(blockState, this.getBodyBlock().defaultBlockState());
        }
        if (this.scheduleFluidTicks) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        return this.canGrowInto(levelReader.getBlockState(blockPos.relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = blockPos.relative(this.growthDirection);
        int i = Math.min(blockState.getValue(AGE) + 1, 25);
        int j = this.getBlocksToGrowWhenBonemealed(randomSource);
        for (int k = 0; k < j && this.canGrowInto(serverLevel.getBlockState(blockPos2)); ++k) {
            serverLevel.setBlockAndUpdate(blockPos2, (BlockState)blockState.setValue(AGE, i));
            blockPos2 = blockPos2.relative(this.growthDirection);
            i = Math.min(i + 1, 25);
        }
    }

    protected abstract int getBlocksToGrowWhenBonemealed(RandomSource var1);

    protected abstract boolean canGrowInto(BlockState var1);

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}

