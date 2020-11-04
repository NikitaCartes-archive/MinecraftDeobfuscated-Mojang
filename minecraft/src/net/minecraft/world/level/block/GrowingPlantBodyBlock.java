package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
	protected GrowingPlantBodyBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean bl) {
		super(properties, direction, voxelShape, bl);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == this.growthDirection.getOpposite() && !blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		GrowingPlantHeadBlock growingPlantHeadBlock = this.getHeadBlock();
		if (direction == this.growthDirection && !blockState2.is(this) && !blockState2.is(growingPlantHeadBlock)) {
			return growingPlantHeadBlock.getStateForPlacement(levelAccessor);
		} else {
			if (this.scheduleFluidTicks) {
				levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
			}

			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(this.getHeadBlock());
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		Optional<BlockPos> optional = this.getHeadPos(blockGetter, blockPos, blockState);
		return optional.isPresent() && this.getHeadBlock().canGrowInto(blockGetter.getBlockState(((BlockPos)optional.get()).relative(this.growthDirection)));
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		Optional<BlockPos> optional = this.getHeadPos(serverLevel, blockPos, blockState);
		if (optional.isPresent()) {
			BlockState blockState2 = serverLevel.getBlockState((BlockPos)optional.get());
			((GrowingPlantHeadBlock)blockState2.getBlock()).performBonemeal(serverLevel, random, (BlockPos)optional.get(), blockState2);
		}
	}

	private Optional<BlockPos> getHeadPos(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		BlockPos blockPos2 = blockPos;

		BlockState blockState2;
		do {
			blockPos2 = blockPos2.relative(this.growthDirection);
			blockState2 = blockGetter.getBlockState(blockPos2);
		} while (blockState2.is(blockState.getBlock()));

		return blockState2.is(this.getHeadBlock()) ? Optional.of(blockPos2) : Optional.empty();
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		boolean bl = super.canBeReplaced(blockState, blockPlaceContext);
		return bl && blockPlaceContext.getItemInHand().is(this.getHeadBlock().asItem()) ? false : bl;
	}

	@Override
	protected Block getBodyBlock() {
		return this;
	}
}
