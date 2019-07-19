/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBlock
extends CrossCollisionBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    private final VoxelShape[] shapeWithPostByIndex;
    private final VoxelShape[] collisionShapeWithPostByIndex;

    public WallBlock(Block.Properties properties) {
        super(0.0f, 3.0f, 0.0f, 14.0f, 24.0f, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(UP, true)).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
        this.shapeWithPostByIndex = this.makeShapes(4.0f, 3.0f, 16.0f, 0.0f, 14.0f);
        this.collisionShapeWithPostByIndex = this.makeShapes(4.0f, 3.0f, 24.0f, 0.0f, 24.0f);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(UP).booleanValue()) {
            return this.shapeWithPostByIndex[this.getAABBIndex(blockState)];
        }
        return super.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(UP).booleanValue()) {
            return this.collisionShapeWithPostByIndex[this.getAABBIndex(blockState)];
        }
        return super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    private boolean connectsTo(BlockState blockState, boolean bl, Direction direction) {
        Block block = blockState.getBlock();
        boolean bl2 = block.is(BlockTags.WALLS) || block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(blockState, direction);
        return !WallBlock.isExceptionForConnection(block) && bl || bl2;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level levelReader = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockState blockState = levelReader.getBlockState(blockPos2);
        BlockState blockState2 = levelReader.getBlockState(blockPos3);
        BlockState blockState3 = levelReader.getBlockState(blockPos4);
        BlockState blockState4 = levelReader.getBlockState(blockPos5);
        boolean bl = this.connectsTo(blockState, blockState.isFaceSturdy(levelReader, blockPos2, Direction.SOUTH), Direction.SOUTH);
        boolean bl2 = this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos3, Direction.WEST), Direction.WEST);
        boolean bl3 = this.connectsTo(blockState3, blockState3.isFaceSturdy(levelReader, blockPos4, Direction.NORTH), Direction.NORTH);
        boolean bl4 = this.connectsTo(blockState4, blockState4.isFaceSturdy(levelReader, blockPos5, Direction.EAST), Direction.EAST);
        boolean bl5 = !(bl && !bl2 && bl3 && !bl4 || !bl && bl2 && !bl3 && bl4);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(UP, bl5 || !levelReader.isEmptyBlock(blockPos.above()))).setValue(NORTH, bl)).setValue(EAST, bl2)).setValue(SOUTH, bl3)).setValue(WEST, bl4)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction == Direction.DOWN) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        Direction direction2 = direction.getOpposite();
        boolean bl = direction == Direction.NORTH ? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2) : blockState.getValue(NORTH).booleanValue();
        boolean bl2 = direction == Direction.EAST ? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2) : blockState.getValue(EAST).booleanValue();
        boolean bl3 = direction == Direction.SOUTH ? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2) : blockState.getValue(SOUTH).booleanValue();
        boolean bl4 = direction == Direction.WEST ? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2) : blockState.getValue(WEST).booleanValue();
        boolean bl5 = !(bl && !bl2 && bl3 && !bl4 || !bl && bl2 && !bl3 && bl4);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(UP, bl5 || !levelAccessor.isEmptyBlock(blockPos.above()))).setValue(NORTH, bl)).setValue(EAST, bl2)).setValue(SOUTH, bl3)).setValue(WEST, bl4);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}

