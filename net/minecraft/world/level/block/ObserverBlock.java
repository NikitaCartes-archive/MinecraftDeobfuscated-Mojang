/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ObserverBlock
extends DirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ObserverBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.SOUTH)).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (blockState.getValue(POWERED).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, false), 2);
        } else {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, true), 2);
            serverLevel.scheduleTick(blockPos, this, 2);
        }
        this.updateNeighborsInFront(serverLevel, blockPos, blockState);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(FACING) == direction && !blockState.getValue(POWERED).booleanValue()) {
            this.startSignal(levelAccessor, blockPos);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    private void startSignal(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!levelAccessor.isClientSide() && !levelAccessor.getBlockTicks().hasScheduledTick(blockPos, this)) {
            levelAccessor.scheduleTick(blockPos, this, 2);
        }
    }

    protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        level.neighborChanged(blockPos2, this, blockPos);
        level.updateNeighborsAtExceptFromFacing(blockPos2, this, direction);
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(POWERED).booleanValue() && blockState.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        if (!level.isClientSide() && blockState.getValue(POWERED).booleanValue() && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            BlockState blockState3 = (BlockState)blockState.setValue(POWERED, false);
            level.setBlock(blockPos, blockState3, 18);
            this.updateNeighborsInFront(level, blockPos, blockState3);
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        if (!level.isClientSide && blockState.getValue(POWERED).booleanValue() && level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            this.updateNeighborsInFront(level, blockPos, (BlockState)blockState.setValue(POWERED, false));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite().getOpposite());
    }
}

