package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class HugeMushroomBlock extends Block {
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	public static final BooleanProperty UP = PipeBlock.UP;
	public static final BooleanProperty DOWN = PipeBlock.DOWN;
	private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

	public HugeMushroomBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, Boolean.valueOf(true))
				.setValue(EAST, Boolean.valueOf(true))
				.setValue(SOUTH, Boolean.valueOf(true))
				.setValue(WEST, Boolean.valueOf(true))
				.setValue(UP, Boolean.valueOf(true))
				.setValue(DOWN, Boolean.valueOf(true))
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockGetter blockGetter = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		return this.defaultBlockState()
			.setValue(DOWN, Boolean.valueOf(this != blockGetter.getBlockState(blockPos.below()).getBlock()))
			.setValue(UP, Boolean.valueOf(this != blockGetter.getBlockState(blockPos.above()).getBlock()))
			.setValue(NORTH, Boolean.valueOf(this != blockGetter.getBlockState(blockPos.north()).getBlock()))
			.setValue(EAST, Boolean.valueOf(this != blockGetter.getBlockState(blockPos.east()).getBlock()))
			.setValue(SOUTH, Boolean.valueOf(this != blockGetter.getBlockState(blockPos.south()).getBlock()))
			.setValue(WEST, Boolean.valueOf(this != blockGetter.getBlockState(blockPos.west()).getBlock()));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return blockState2.getBlock() == this
			? blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(false))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.NORTH)), blockState.getValue(NORTH))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.SOUTH)), blockState.getValue(SOUTH))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.EAST)), blockState.getValue(EAST))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.WEST)), blockState.getValue(WEST))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.UP)), blockState.getValue(UP))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.DOWN)), blockState.getValue(DOWN));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.NORTH)), blockState.getValue(NORTH))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.SOUTH)), blockState.getValue(SOUTH))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.EAST)), blockState.getValue(EAST))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.WEST)), blockState.getValue(WEST))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.UP)), blockState.getValue(UP))
			.setValue((Property)PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.DOWN)), blockState.getValue(DOWN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
	}
}
