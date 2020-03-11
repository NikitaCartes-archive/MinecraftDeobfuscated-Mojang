package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

	protected KelpBlock(Block.Properties properties) {
		super(properties, Direction.UP, SHAPE, true, 0.14);
	}

	@Override
	protected boolean canGrowInto(BlockState blockState) {
		return blockState.getBlock() == Blocks.WATER;
	}

	@Override
	protected Block getBodyBlock() {
		return Blocks.KELP_PLANT;
	}

	@Override
	protected boolean canAttachToBlock(Block block) {
		return block != Blocks.MAGMA_BLOCK;
	}

	@Override
	public boolean canPlaceLiquid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
		return false;
	}

	@Override
	public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		return false;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		return fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8 ? this.getStateForPlacement(blockPlaceContext.getLevel()) : null;
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return Fluids.WATER.getSource(false);
	}
}
