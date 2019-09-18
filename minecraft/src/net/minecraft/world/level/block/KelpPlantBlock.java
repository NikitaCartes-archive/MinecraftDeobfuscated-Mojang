package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class KelpPlantBlock extends Block implements LiquidBlockContainer {
	private final KelpBlock top;

	protected KelpPlantBlock(KelpBlock kelpBlock, Block.Properties properties) {
		super(properties);
		this.top = kelpBlock;
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return Fluids.WATER.getSource(false);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}

		super.tick(blockState, serverLevel, blockPos, random);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		if (direction == Direction.UP) {
			Block block = blockState2.getBlock();
			if (block != this && block != this.top) {
				return this.top.getStateForPlacement(levelAccessor);
			}
		}

		levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		Block block = blockState2.getBlock();
		return block != Blocks.MAGMA_BLOCK && (block == this || blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(Blocks.KELP);
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
