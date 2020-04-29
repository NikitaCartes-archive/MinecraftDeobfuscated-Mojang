/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SlabBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public SlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM)).setValue(WATERLOGGED, false));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return blockState.getValue(TYPE) != SlabType.DOUBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        SlabType slabType = blockState.getValue(TYPE);
        switch (slabType) {
            case DOUBLE: {
                return Shapes.block();
            }
            case TOP: {
                return TOP_AABB;
            }
        }
        return BOTTOM_AABB;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPos);
        if (blockState.is(this)) {
            return (BlockState)((BlockState)blockState.setValue(TYPE, SlabType.DOUBLE)).setValue(WATERLOGGED, false);
        }
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
        BlockState blockState2 = (BlockState)((BlockState)this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        Direction direction = blockPlaceContext.getClickedFace();
        if (direction == Direction.DOWN || direction != Direction.UP && blockPlaceContext.getClickLocation().y - (double)blockPos.getY() > 0.5) {
            return (BlockState)blockState2.setValue(TYPE, SlabType.TOP);
        }
        return blockState2;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        ItemStack itemStack = blockPlaceContext.getItemInHand();
        SlabType slabType = blockState.getValue(TYPE);
        if (slabType == SlabType.DOUBLE || itemStack.getItem() != this.asItem()) {
            return false;
        }
        if (blockPlaceContext.replacingClickedOnBlock()) {
            boolean bl = blockPlaceContext.getClickLocation().y - (double)blockPlaceContext.getClickedPos().getY() > 0.5;
            Direction direction = blockPlaceContext.getClickedFace();
            if (slabType == SlabType.BOTTOM) {
                return direction == Direction.UP || bl && direction.getAxis().isHorizontal();
            }
            return direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal();
        }
        return true;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.getValue(TYPE) != SlabType.DOUBLE) {
            return SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
        }
        return false;
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        if (blockState.getValue(TYPE) != SlabType.DOUBLE) {
            return SimpleWaterloggedBlock.super.canPlaceLiquid(blockGetter, blockPos, blockState, fluid);
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND: {
                return false;
            }
            case WATER: {
                return blockGetter.getFluidState(blockPos).is(FluidTags.WATER);
            }
            case AIR: {
                return false;
            }
        }
        return false;
    }
}

