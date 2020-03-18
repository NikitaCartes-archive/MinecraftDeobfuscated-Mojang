/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class HugeMushroomBlock
extends Block {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

    public HugeMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, true)).setValue(EAST, true)).setValue(SOUTH, true)).setValue(WEST, true)).setValue(UP, true)).setValue(DOWN, true));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level blockGetter = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(DOWN, this != blockGetter.getBlockState(blockPos.below()).getBlock())).setValue(UP, this != blockGetter.getBlockState(blockPos.above()).getBlock())).setValue(NORTH, this != blockGetter.getBlockState(blockPos.north()).getBlock())).setValue(EAST, this != blockGetter.getBlockState(blockPos.east()).getBlock())).setValue(SOUTH, this != blockGetter.getBlockState(blockPos.south()).getBlock())).setValue(WEST, this != blockGetter.getBlockState(blockPos.west()).getBlock());
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState2.getBlock() == this) {
            return (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), false);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.NORTH)), blockState.getValue(NORTH))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.SOUTH)), blockState.getValue(SOUTH))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.EAST)), blockState.getValue(EAST))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.WEST)), blockState.getValue(WEST))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.UP)), blockState.getValue(UP))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.DOWN)), blockState.getValue(DOWN));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.NORTH)), blockState.getValue(NORTH))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.SOUTH)), blockState.getValue(SOUTH))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.EAST)), blockState.getValue(EAST))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.WEST)), blockState.getValue(WEST))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.UP)), blockState.getValue(UP))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.DOWN)), blockState.getValue(DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }
}

