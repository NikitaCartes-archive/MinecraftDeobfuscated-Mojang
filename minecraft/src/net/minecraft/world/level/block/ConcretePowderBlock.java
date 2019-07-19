package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
	private final BlockState concrete;

	public ConcretePowderBlock(Block block, Block.Properties properties) {
		super(properties);
		this.concrete = block.defaultBlockState();
	}

	@Override
	public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		if (canSolidify(blockState2)) {
			level.setBlock(blockPos, this.concrete, 3);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockGetter blockGetter = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		return !canSolidify(blockGetter.getBlockState(blockPos)) && !touchesLiquid(blockGetter, blockPos)
			? super.getStateForPlacement(blockPlaceContext)
			: this.concrete;
	}

	private static boolean touchesLiquid(BlockGetter blockGetter, BlockPos blockPos) {
		boolean bl = false;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);

		for (Direction direction : Direction.values()) {
			BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
			if (direction != Direction.DOWN || canSolidify(blockState)) {
				mutableBlockPos.set(blockPos).move(direction);
				blockState = blockGetter.getBlockState(mutableBlockPos);
				if (canSolidify(blockState) && !blockState.isFaceSturdy(blockGetter, blockPos, direction.getOpposite())) {
					bl = true;
					break;
				}
			}
		}

		return bl;
	}

	private static boolean canSolidify(BlockState blockState) {
		return blockState.getFluidState().is(FluidTags.WATER);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return touchesLiquid(levelAccessor, blockPos) ? this.concrete : super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}
}
