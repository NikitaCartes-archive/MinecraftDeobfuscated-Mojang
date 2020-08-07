package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ChorusPlantBlock extends PipeBlock {
	protected ChorusPlantBlock(BlockBehaviour.Properties properties) {
		super(0.3125F, properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(UP, Boolean.valueOf(false))
				.setValue(DOWN, Boolean.valueOf(false))
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.getStateForPlacement(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
	}

	public BlockState getStateForPlacement(BlockGetter blockGetter, BlockPos blockPos) {
		Block block = blockGetter.getBlockState(blockPos.below()).getBlock();
		Block block2 = blockGetter.getBlockState(blockPos.above()).getBlock();
		Block block3 = blockGetter.getBlockState(blockPos.north()).getBlock();
		Block block4 = blockGetter.getBlockState(blockPos.east()).getBlock();
		Block block5 = blockGetter.getBlockState(blockPos.south()).getBlock();
		Block block6 = blockGetter.getBlockState(blockPos.west()).getBlock();
		return this.defaultBlockState()
			.setValue(DOWN, Boolean.valueOf(block == this || block == Blocks.CHORUS_FLOWER || block == Blocks.END_STONE))
			.setValue(UP, Boolean.valueOf(block2 == this || block2 == Blocks.CHORUS_FLOWER))
			.setValue(NORTH, Boolean.valueOf(block3 == this || block3 == Blocks.CHORUS_FLOWER))
			.setValue(EAST, Boolean.valueOf(block4 == this || block4 == Blocks.CHORUS_FLOWER))
			.setValue(SOUTH, Boolean.valueOf(block5 == this || block5 == Blocks.CHORUS_FLOWER))
			.setValue(WEST, Boolean.valueOf(block6 == this || block6 == Blocks.CHORUS_FLOWER));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			boolean bl = blockState2.getBlock() == this || blockState2.is(Blocks.CHORUS_FLOWER) || direction == Direction.DOWN && blockState2.is(Blocks.END_STONE);
			return blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(bl));
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		boolean bl = !levelReader.getBlockState(blockPos.above()).isAir() && !blockState2.isAir();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			Block block = levelReader.getBlockState(blockPos2).getBlock();
			if (block == this) {
				if (bl) {
					return false;
				}

				Block block2 = levelReader.getBlockState(blockPos2.below()).getBlock();
				if (block2 == this || block2 == Blocks.END_STONE) {
					return true;
				}
			}
		}

		Block block3 = blockState2.getBlock();
		return block3 == this || block3 == Blocks.END_STONE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
