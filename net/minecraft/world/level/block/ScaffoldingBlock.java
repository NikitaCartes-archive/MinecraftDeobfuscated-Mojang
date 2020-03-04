/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldingBlock
extends Block
implements SimpleWaterloggedBlock {
    private static final VoxelShape STABLE_SHAPE;
    private static final VoxelShape UNSTABLE_SHAPE;
    private static final VoxelShape UNSTABLE_SHAPE_BOTTOM;
    private static final VoxelShape BELOW_BLOCK;
    public static final IntegerProperty DISTANCE;
    public static final BooleanProperty WATERLOGGED;
    public static final BooleanProperty BOTTOM;

    protected ScaffoldingBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(DISTANCE, 7)).setValue(WATERLOGGED, false)).setValue(BOTTOM, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, WATERLOGGED, BOTTOM);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!collisionContext.isHoldingItem(blockState.getBlock().asItem())) {
            return blockState.getValue(BOTTOM) != false ? UNSTABLE_SHAPE : STABLE_SHAPE;
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.block();
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockPlaceContext.getItemInHand().getItem() == this.asItem();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        int i = ScaffoldingBlock.getDistance(level, blockPos);
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, level.getFluidState(blockPos).getType() == Fluids.WATER)).setValue(DISTANCE, i)).setValue(BOTTOM, this.isBottom(level, blockPos, i));
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!level.isClientSide) {
            level.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (!levelAccessor.isClientSide()) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        return blockState;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int i = ScaffoldingBlock.getDistance(serverLevel, blockPos);
        BlockState blockState2 = (BlockState)((BlockState)blockState.setValue(DISTANCE, i)).setValue(BOTTOM, this.isBottom(serverLevel, blockPos, i));
        if (blockState2.getValue(DISTANCE) == 7) {
            if (blockState.getValue(DISTANCE) == 7) {
                serverLevel.addFreshEntity(new FallingBlockEntity(serverLevel, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, (BlockState)blockState2.setValue(WATERLOGGED, false)));
            } else {
                serverLevel.destroyBlock(blockPos, true);
            }
        } else if (blockState != blockState2) {
            serverLevel.setBlock(blockPos, blockState2, 3);
        }
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return ScaffoldingBlock.getDistance(levelReader, blockPos) < 7;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!collisionContext.isAbove(Shapes.block(), blockPos, true) || collisionContext.isDescending()) {
            if (blockState.getValue(DISTANCE) != 0 && blockState.getValue(BOTTOM).booleanValue() && collisionContext.isAbove(BELOW_BLOCK, blockPos, true)) {
                return UNSTABLE_SHAPE_BOTTOM;
            }
            return Shapes.empty();
        }
        return STABLE_SHAPE;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    private boolean isBottom(BlockGetter blockGetter, BlockPos blockPos, int i) {
        return i > 0 && blockGetter.getBlockState(blockPos.below()).getBlock() != this;
    }

    public static int getDistance(BlockGetter blockGetter, BlockPos blockPos) {
        Direction direction;
        BlockState blockState2;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
        BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
        int i = 7;
        if (blockState.getBlock() == Blocks.SCAFFOLDING) {
            i = blockState.getValue(DISTANCE);
        } else if (blockState.isFaceSturdy(blockGetter, mutableBlockPos, Direction.UP)) {
            return 0;
        }
        Iterator<Direction> iterator = Direction.Plane.HORIZONTAL.iterator();
        while (iterator.hasNext() && ((blockState2 = blockGetter.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction = iterator.next()))).getBlock() != Blocks.SCAFFOLDING || (i = Math.min(i, blockState2.getValue(DISTANCE) + 1)) != 1)) {
        }
        return i;
    }

    static {
        UNSTABLE_SHAPE_BOTTOM = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
        BELOW_BLOCK = Shapes.block().move(0.0, -1.0, 0.0);
        DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        BOTTOM = BlockStateProperties.BOTTOM;
        VoxelShape voxelShape = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
        VoxelShape voxelShape2 = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0);
        VoxelShape voxelShape3 = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0);
        VoxelShape voxelShape4 = Block.box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0);
        VoxelShape voxelShape5 = Block.box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0);
        STABLE_SHAPE = Shapes.or(voxelShape, voxelShape2, voxelShape3, voxelShape4, voxelShape5);
        VoxelShape voxelShape6 = Block.box(0.0, 0.0, 0.0, 2.0, 2.0, 16.0);
        VoxelShape voxelShape7 = Block.box(14.0, 0.0, 0.0, 16.0, 2.0, 16.0);
        VoxelShape voxelShape8 = Block.box(0.0, 0.0, 14.0, 16.0, 2.0, 16.0);
        VoxelShape voxelShape9 = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 2.0);
        UNSTABLE_SHAPE = Shapes.or(UNSTABLE_SHAPE_BOTTOM, STABLE_SHAPE, voxelShape7, voxelShape6, voxelShape9, voxelShape8);
    }
}

