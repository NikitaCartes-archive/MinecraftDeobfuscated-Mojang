package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseCoralWallFanBlock extends BaseCoralFanBlock {
	public static final MapCodec<BaseCoralWallFanBlock> CODEC = simpleCodec(BaseCoralWallFanBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Block.box(0.0, 4.0, 5.0, 16.0, 12.0, 16.0),
			Direction.SOUTH,
			Block.box(0.0, 4.0, 0.0, 16.0, 12.0, 11.0),
			Direction.WEST,
			Block.box(5.0, 4.0, 0.0, 16.0, 12.0, 16.0),
			Direction.EAST,
			Block.box(0.0, 4.0, 0.0, 11.0, 12.0, 16.0)
		)
	);

	@Override
	public MapCodec<? extends BaseCoralWallFanBlock> codec() {
		return CODEC;
	}

	protected BaseCoralWallFanBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(true)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)SHAPES.get(blockState.getValue(FACING));
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: blockState;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Direction direction = blockState.getValue(FACING);
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return blockState2.isFaceSturdy(levelReader, blockPos2, direction);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getStateForPlacement(blockPlaceContext);
		LevelReader levelReader = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction[] directions = blockPlaceContext.getNearestLookingDirections();

		for (Direction direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				blockState = blockState.setValue(FACING, direction.getOpposite());
				if (blockState.canSurvive(levelReader, blockPos)) {
					return blockState;
				}
			}
		}

		return null;
	}
}
