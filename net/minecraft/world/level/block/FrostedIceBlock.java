/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FrostedIceBlock
extends IceBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final int NEIGHBORS_TO_AGE = 4;
    private static final int NEIGHBORS_TO_MELT = 2;

    public FrostedIceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        this.tick(blockState, serverLevel, blockPos, randomSource);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if ((randomSource.nextInt(3) == 0 || this.fewerNeigboursThan(serverLevel, blockPos, 4)) && serverLevel.getMaxLocalRawBrightness(blockPos) > 11 - blockState.getValue(AGE) - blockState.getLightBlock(serverLevel, blockPos) && this.slightlyMelt(blockState, serverLevel, blockPos)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
                if (!blockState2.is(this) || this.slightlyMelt(blockState2, serverLevel, mutableBlockPos)) continue;
                serverLevel.scheduleTick((BlockPos)mutableBlockPos, this, Mth.nextInt(randomSource, 20, 40));
            }
            return;
        }
        serverLevel.scheduleTick(blockPos, this, Mth.nextInt(randomSource, 20, 40));
    }

    private boolean slightlyMelt(BlockState blockState, Level level, BlockPos blockPos) {
        int i = blockState.getValue(AGE);
        if (i < 3) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(AGE, i + 1), 2);
            return false;
        }
        this.melt(blockState, level, blockPos);
        return true;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (block.defaultBlockState().is(this) && this.fewerNeigboursThan(level, blockPos, 2)) {
            this.melt(blockState, level, blockPos);
        }
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
    }

    private boolean fewerNeigboursThan(BlockGetter blockGetter, BlockPos blockPos, int i) {
        int j = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            if (!blockGetter.getBlockState(mutableBlockPos).is(this) || ++j < i) continue;
            return false;
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }
}

