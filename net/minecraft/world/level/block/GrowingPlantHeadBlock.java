/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;

public abstract class GrowingPlantHeadBlock
extends GrowingPlantBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    private final double growPerTickProbability;

    protected GrowingPlantHeadBlock(Block.Properties properties, Direction direction, boolean bl, double d) {
        super(properties, direction, bl);
        this.growPerTickProbability = d;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    public BlockState getStateForPlacement(LevelAccessor levelAccessor) {
        return (BlockState)this.defaultBlockState().setValue(AGE, levelAccessor.getRandom().nextInt(25));
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BlockPos blockPos2;
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
            return;
        }
        if (blockState.getValue(AGE) < 25 && random.nextDouble() < this.growPerTickProbability && this.canGrowInto(serverLevel.getBlockState(blockPos2 = blockPos.relative(this.growthDirection)))) {
            serverLevel.setBlockAndUpdate(blockPos2, (BlockState)blockState.cycle(AGE));
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == this.growthDirection.getOpposite() && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        if (direction == this.growthDirection && blockState2.getBlock() == this) {
            return this.getBodyBlock().defaultBlockState();
        }
        if (this.scheduleFluidTicks) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    protected abstract boolean canGrowInto(BlockState var1);

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}

