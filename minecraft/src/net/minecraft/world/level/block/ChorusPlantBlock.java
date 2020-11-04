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
		BlockState blockState = blockGetter.getBlockState(blockPos.below());
		BlockState blockState2 = blockGetter.getBlockState(blockPos.above());
		BlockState blockState3 = blockGetter.getBlockState(blockPos.north());
		BlockState blockState4 = blockGetter.getBlockState(blockPos.east());
		BlockState blockState5 = blockGetter.getBlockState(blockPos.south());
		BlockState blockState6 = blockGetter.getBlockState(blockPos.west());
		return this.defaultBlockState()
			.setValue(DOWN, Boolean.valueOf(blockState.is(this) || blockState.is(Blocks.CHORUS_FLOWER) || blockState.is(Blocks.END_STONE)))
			.setValue(UP, Boolean.valueOf(blockState2.is(this) || blockState2.is(Blocks.CHORUS_FLOWER)))
			.setValue(NORTH, Boolean.valueOf(blockState3.is(this) || blockState3.is(Blocks.CHORUS_FLOWER)))
			.setValue(EAST, Boolean.valueOf(blockState4.is(this) || blockState4.is(Blocks.CHORUS_FLOWER)))
			.setValue(SOUTH, Boolean.valueOf(blockState5.is(this) || blockState5.is(Blocks.CHORUS_FLOWER)))
			.setValue(WEST, Boolean.valueOf(blockState6.is(this) || blockState6.is(Blocks.CHORUS_FLOWER)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			boolean bl = blockState2.is(this) || blockState2.is(Blocks.CHORUS_FLOWER) || direction == Direction.DOWN && blockState2.is(Blocks.END_STONE);
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
			BlockState blockState3 = levelReader.getBlockState(blockPos2);
			if (blockState3.is(this)) {
				if (bl) {
					return false;
				}

				BlockState blockState4 = levelReader.getBlockState(blockPos2.below());
				if (blockState4.is(this) || blockState4.is(Blocks.END_STONE)) {
					return true;
				}
			}
		}

		return blockState2.is(this) || blockState2.is(Blocks.END_STONE);
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
