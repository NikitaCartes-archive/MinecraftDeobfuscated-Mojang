/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TallSeagrassBlock
extends DoublePlantBlock
implements LiquidBlockContainer {
    public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;
    protected static final float AABB_OFFSET = 6.0f;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public TallSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) && !blockState.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(Blocks.SEAGRASS);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState;
        BlockState blockState = super.getStateForPlacement(blockPlaceContext);
        if (blockState != null && (fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos().above())).is(FluidTags.WATER) && fluidState.getAmount() == 8) {
            return blockState;
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState blockState2 = levelReader.getBlockState(blockPos.below());
            return blockState2.is(this) && blockState2.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        FluidState fluidState = levelReader.getFluidState(blockPos);
        return super.canSurvive(blockState, levelReader, blockPos) && fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        return false;
    }
}

