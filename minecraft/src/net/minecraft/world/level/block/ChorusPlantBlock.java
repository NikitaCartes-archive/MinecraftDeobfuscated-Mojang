package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
	public static final MapCodec<ChorusPlantBlock> CODEC = simpleCodec(ChorusPlantBlock::new);

	@Override
	public MapCodec<ChorusPlantBlock> codec() {
		return CODEC;
	}

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
		return getStateWithConnections(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState());
	}

	public static BlockState getStateWithConnections(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		BlockState blockState2 = blockGetter.getBlockState(blockPos.below());
		BlockState blockState3 = blockGetter.getBlockState(blockPos.above());
		BlockState blockState4 = blockGetter.getBlockState(blockPos.north());
		BlockState blockState5 = blockGetter.getBlockState(blockPos.east());
		BlockState blockState6 = blockGetter.getBlockState(blockPos.south());
		BlockState blockState7 = blockGetter.getBlockState(blockPos.west());
		Block block = blockState.getBlock();
		return blockState.trySetValue(DOWN, Boolean.valueOf(blockState2.is(block) || blockState2.is(Blocks.CHORUS_FLOWER) || blockState2.is(Blocks.END_STONE)))
			.trySetValue(UP, Boolean.valueOf(blockState3.is(block) || blockState3.is(Blocks.CHORUS_FLOWER)))
			.trySetValue(NORTH, Boolean.valueOf(blockState4.is(block) || blockState4.is(Blocks.CHORUS_FLOWER)))
			.trySetValue(EAST, Boolean.valueOf(blockState5.is(block) || blockState5.is(Blocks.CHORUS_FLOWER)))
			.trySetValue(SOUTH, Boolean.valueOf(blockState6.is(block) || blockState6.is(Blocks.CHORUS_FLOWER)))
			.trySetValue(WEST, Boolean.valueOf(blockState7.is(block) || blockState7.is(Blocks.CHORUS_FLOWER)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.scheduleTick(blockPos, this, 1);
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			boolean bl = blockState2.is(this) || blockState2.is(Blocks.CHORUS_FLOWER) || direction == Direction.DOWN && blockState2.is(Blocks.END_STONE);
			return blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(bl));
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
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
