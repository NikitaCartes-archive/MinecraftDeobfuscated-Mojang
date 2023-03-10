/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeavesBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final int DECAY_DISTANCE = 7;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final int TICK_DELAY = 1;

    public LeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(DISTANCE, 7)).setValue(PERSISTENT, false)).setValue(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(DISTANCE) == 7 && blockState.getValue(PERSISTENT) == false;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (this.decaying(blockState)) {
            LeavesBlock.dropResources(blockState, serverLevel, blockPos);
            serverLevel.removeBlock(blockPos, false);
        }
    }

    protected boolean decaying(BlockState blockState) {
        return blockState.getValue(PERSISTENT) == false && blockState.getValue(DISTANCE) == 7;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        serverLevel.setBlock(blockPos, LeavesBlock.updateDistance(blockState, serverLevel, blockPos), 3);
    }

    @Override
    public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        int i;
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if ((i = LeavesBlock.getDistanceAt(blockState2) + 1) != 1 || blockState.getValue(DISTANCE) != i) {
            levelAccessor.scheduleTick(blockPos, this, 1);
        }
        return blockState;
    }

    private static BlockState updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int i = 7;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            i = Math.min(i, LeavesBlock.getDistanceAt(levelAccessor.getBlockState(mutableBlockPos)) + 1);
            if (i == 1) break;
        }
        return (BlockState)blockState.setValue(DISTANCE, i);
    }

    private static int getDistanceAt(BlockState blockState) {
        if (blockState.is(BlockTags.LOGS)) {
            return 0;
        }
        if (blockState.getBlock() instanceof LeavesBlock) {
            return blockState.getValue(DISTANCE);
        }
        return 7;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!level.isRainingAt(blockPos.above())) {
            return;
        }
        if (randomSource.nextInt(15) != 1) {
            return;
        }
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState2.canOcclude() && blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) {
            return;
        }
        ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, ParticleTypes.DRIPPING_WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(PERSISTENT, true)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        return LeavesBlock.updateDistance(blockState, blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }
}

