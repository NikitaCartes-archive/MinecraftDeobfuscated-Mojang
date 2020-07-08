package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final EnumProperty<WallSide> EAST_WALL = BlockStateProperties.EAST_WALL;
	public static final EnumProperty<WallSide> NORTH_WALL = BlockStateProperties.NORTH_WALL;
	public static final EnumProperty<WallSide> SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
	public static final EnumProperty<WallSide> WEST_WALL = BlockStateProperties.WEST_WALL;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private final Map<BlockState, VoxelShape> shapeByIndex;
	private final Map<BlockState, VoxelShape> collisionShapeByIndex;
	private static final VoxelShape POST_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
	private static final VoxelShape NORTH_TEST = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 9.0);
	private static final VoxelShape SOUTH_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 16.0);
	private static final VoxelShape WEST_TEST = Block.box(0.0, 0.0, 7.0, 9.0, 16.0, 9.0);
	private static final VoxelShape EAST_TEST = Block.box(7.0, 0.0, 7.0, 16.0, 16.0, 9.0);

	public WallBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(UP, Boolean.valueOf(true))
				.setValue(NORTH_WALL, WallSide.NONE)
				.setValue(EAST_WALL, WallSide.NONE)
				.setValue(SOUTH_WALL, WallSide.NONE)
				.setValue(WEST_WALL, WallSide.NONE)
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
		this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
		this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
	}

	private static VoxelShape applyWallShape(VoxelShape voxelShape, WallSide wallSide, VoxelShape voxelShape2, VoxelShape voxelShape3) {
		if (wallSide == WallSide.TALL) {
			return Shapes.or(voxelShape, voxelShape3);
		} else {
			return wallSide == WallSide.LOW ? Shapes.or(voxelShape, voxelShape2) : voxelShape;
		}
	}

	private Map<BlockState, VoxelShape> makeShapes(float f, float g, float h, float i, float j, float k) {
		float l = 8.0F - f;
		float m = 8.0F + f;
		float n = 8.0F - g;
		float o = 8.0F + g;
		VoxelShape voxelShape = Block.box((double)l, 0.0, (double)l, (double)m, (double)h, (double)m);
		VoxelShape voxelShape2 = Block.box((double)n, (double)i, 0.0, (double)o, (double)j, (double)o);
		VoxelShape voxelShape3 = Block.box((double)n, (double)i, (double)n, (double)o, (double)j, 16.0);
		VoxelShape voxelShape4 = Block.box(0.0, (double)i, (double)n, (double)o, (double)j, (double)o);
		VoxelShape voxelShape5 = Block.box((double)n, (double)i, (double)n, 16.0, (double)j, (double)o);
		VoxelShape voxelShape6 = Block.box((double)n, (double)i, 0.0, (double)o, (double)k, (double)o);
		VoxelShape voxelShape7 = Block.box((double)n, (double)i, (double)n, (double)o, (double)k, 16.0);
		VoxelShape voxelShape8 = Block.box(0.0, (double)i, (double)n, (double)o, (double)k, (double)o);
		VoxelShape voxelShape9 = Block.box((double)n, (double)i, (double)n, 16.0, (double)k, (double)o);
		Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

		for (Boolean boolean_ : UP.getPossibleValues()) {
			for (WallSide wallSide : EAST_WALL.getPossibleValues()) {
				for (WallSide wallSide2 : NORTH_WALL.getPossibleValues()) {
					for (WallSide wallSide3 : WEST_WALL.getPossibleValues()) {
						for (WallSide wallSide4 : SOUTH_WALL.getPossibleValues()) {
							VoxelShape voxelShape10 = Shapes.empty();
							voxelShape10 = applyWallShape(voxelShape10, wallSide, voxelShape5, voxelShape9);
							voxelShape10 = applyWallShape(voxelShape10, wallSide3, voxelShape4, voxelShape8);
							voxelShape10 = applyWallShape(voxelShape10, wallSide2, voxelShape2, voxelShape6);
							voxelShape10 = applyWallShape(voxelShape10, wallSide4, voxelShape3, voxelShape7);
							if (boolean_) {
								voxelShape10 = Shapes.or(voxelShape10, voxelShape);
							}

							BlockState blockState = this.defaultBlockState()
								.setValue(UP, boolean_)
								.setValue(EAST_WALL, wallSide)
								.setValue(WEST_WALL, wallSide3)
								.setValue(NORTH_WALL, wallSide2)
								.setValue(SOUTH_WALL, wallSide4);
							builder.put(blockState.setValue(WATERLOGGED, Boolean.valueOf(false)), voxelShape10);
							builder.put(blockState.setValue(WATERLOGGED, Boolean.valueOf(true)), voxelShape10);
						}
					}
				}
			}
		}

		return builder.build();
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.shapeByIndex.get(blockState);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.collisionShapeByIndex.get(blockState);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	private boolean connectsTo(BlockState blockState, boolean bl, Direction direction) {
		Block block = blockState.getBlock();
		boolean bl2 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(blockState, direction);
		return blockState.is(BlockTags.WALLS) || !isExceptionForConnection(block) && bl || block instanceof IronBarsBlock || bl2;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		LevelReader levelReader = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		BlockPos blockPos2 = blockPos.north();
		BlockPos blockPos3 = blockPos.east();
		BlockPos blockPos4 = blockPos.south();
		BlockPos blockPos5 = blockPos.west();
		BlockPos blockPos6 = blockPos.above();
		BlockState blockState = levelReader.getBlockState(blockPos2);
		BlockState blockState2 = levelReader.getBlockState(blockPos3);
		BlockState blockState3 = levelReader.getBlockState(blockPos4);
		BlockState blockState4 = levelReader.getBlockState(blockPos5);
		BlockState blockState5 = levelReader.getBlockState(blockPos6);
		boolean bl = this.connectsTo(blockState, blockState.isFaceSturdy(levelReader, blockPos2, Direction.SOUTH), Direction.SOUTH);
		boolean bl2 = this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos3, Direction.WEST), Direction.WEST);
		boolean bl3 = this.connectsTo(blockState3, blockState3.isFaceSturdy(levelReader, blockPos4, Direction.NORTH), Direction.NORTH);
		boolean bl4 = this.connectsTo(blockState4, blockState4.isFaceSturdy(levelReader, blockPos5, Direction.EAST), Direction.EAST);
		BlockState blockState6 = this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
		return this.updateShape(levelReader, blockState6, blockPos6, blockState5, bl, bl2, bl3, bl4);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		if (direction == Direction.DOWN) {
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			return direction == Direction.UP
				? this.topUpdate(levelAccessor, blockState, blockPos2, blockState2)
				: this.sideUpdate(levelAccessor, blockPos, blockState, blockPos2, blockState2, direction);
		}
	}

	private static boolean isConnected(BlockState blockState, Property<WallSide> property) {
		return blockState.getValue(property) != WallSide.NONE;
	}

	private static boolean isCovered(VoxelShape voxelShape, VoxelShape voxelShape2) {
		return !Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.ONLY_FIRST);
	}

	private BlockState topUpdate(LevelReader levelReader, BlockState blockState, BlockPos blockPos, BlockState blockState2) {
		boolean bl = isConnected(blockState, NORTH_WALL);
		boolean bl2 = isConnected(blockState, EAST_WALL);
		boolean bl3 = isConnected(blockState, SOUTH_WALL);
		boolean bl4 = isConnected(blockState, WEST_WALL);
		return this.updateShape(levelReader, blockState, blockPos, blockState2, bl, bl2, bl3, bl4);
	}

	private BlockState sideUpdate(
		LevelReader levelReader, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, Direction direction
	) {
		Direction direction2 = direction.getOpposite();
		boolean bl = direction == Direction.NORTH
			? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, direction2), direction2)
			: isConnected(blockState, NORTH_WALL);
		boolean bl2 = direction == Direction.EAST
			? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, direction2), direction2)
			: isConnected(blockState, EAST_WALL);
		boolean bl3 = direction == Direction.SOUTH
			? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, direction2), direction2)
			: isConnected(blockState, SOUTH_WALL);
		boolean bl4 = direction == Direction.WEST
			? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, direction2), direction2)
			: isConnected(blockState, WEST_WALL);
		BlockPos blockPos3 = blockPos.above();
		BlockState blockState3 = levelReader.getBlockState(blockPos3);
		return this.updateShape(levelReader, blockState, blockPos3, blockState3, bl, bl2, bl3, bl4);
	}

	private BlockState updateShape(
		LevelReader levelReader, BlockState blockState, BlockPos blockPos, BlockState blockState2, boolean bl, boolean bl2, boolean bl3, boolean bl4
	) {
		VoxelShape voxelShape = blockState2.getCollisionShape(levelReader, blockPos).getFaceShape(Direction.DOWN);
		BlockState blockState3 = this.updateSides(blockState, bl, bl2, bl3, bl4, voxelShape);
		return blockState3.setValue(UP, Boolean.valueOf(this.shouldRaisePost(blockState3, blockState2, voxelShape)));
	}

	private boolean shouldRaisePost(BlockState blockState, BlockState blockState2, VoxelShape voxelShape) {
		boolean bl = blockState2.getBlock() instanceof WallBlock && (Boolean)blockState2.getValue(UP);
		if (bl) {
			return true;
		} else {
			WallSide wallSide = blockState.getValue(NORTH_WALL);
			WallSide wallSide2 = blockState.getValue(SOUTH_WALL);
			WallSide wallSide3 = blockState.getValue(EAST_WALL);
			WallSide wallSide4 = blockState.getValue(WEST_WALL);
			boolean bl2 = wallSide2 == WallSide.NONE;
			boolean bl3 = wallSide4 == WallSide.NONE;
			boolean bl4 = wallSide3 == WallSide.NONE;
			boolean bl5 = wallSide == WallSide.NONE;
			boolean bl6 = bl5 && bl2 && bl3 && bl4 || bl5 != bl2 || bl3 != bl4;
			if (bl6) {
				return true;
			} else {
				boolean bl7 = wallSide == WallSide.TALL && wallSide2 == WallSide.TALL || wallSide3 == WallSide.TALL && wallSide4 == WallSide.TALL;
				return bl7 ? false : blockState2.getBlock().is(BlockTags.WALL_POST_OVERRIDE) || isCovered(voxelShape, POST_TEST);
			}
		}
	}

	private BlockState updateSides(BlockState blockState, boolean bl, boolean bl2, boolean bl3, boolean bl4, VoxelShape voxelShape) {
		return blockState.setValue(NORTH_WALL, this.makeWallState(bl, voxelShape, NORTH_TEST))
			.setValue(EAST_WALL, this.makeWallState(bl2, voxelShape, EAST_TEST))
			.setValue(SOUTH_WALL, this.makeWallState(bl3, voxelShape, SOUTH_TEST))
			.setValue(WEST_WALL, this.makeWallState(bl4, voxelShape, WEST_TEST));
	}

	private WallSide makeWallState(boolean bl, VoxelShape voxelShape, VoxelShape voxelShape2) {
		if (bl) {
			return isCovered(voxelShape, voxelShape2) ? WallSide.TALL : WallSide.LOW;
		} else {
			return WallSide.NONE;
		}
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return !(Boolean)blockState.getValue(WATERLOGGED);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL, WATERLOGGED);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_180:
				return blockState.setValue(NORTH_WALL, blockState.getValue(SOUTH_WALL))
					.setValue(EAST_WALL, blockState.getValue(WEST_WALL))
					.setValue(SOUTH_WALL, blockState.getValue(NORTH_WALL))
					.setValue(WEST_WALL, blockState.getValue(EAST_WALL));
			case COUNTERCLOCKWISE_90:
				return blockState.setValue(NORTH_WALL, blockState.getValue(EAST_WALL))
					.setValue(EAST_WALL, blockState.getValue(SOUTH_WALL))
					.setValue(SOUTH_WALL, blockState.getValue(WEST_WALL))
					.setValue(WEST_WALL, blockState.getValue(NORTH_WALL));
			case CLOCKWISE_90:
				return blockState.setValue(NORTH_WALL, blockState.getValue(WEST_WALL))
					.setValue(EAST_WALL, blockState.getValue(NORTH_WALL))
					.setValue(SOUTH_WALL, blockState.getValue(EAST_WALL))
					.setValue(WEST_WALL, blockState.getValue(SOUTH_WALL));
			default:
				return blockState;
		}
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		switch (mirror) {
			case LEFT_RIGHT:
				return blockState.setValue(NORTH_WALL, blockState.getValue(SOUTH_WALL)).setValue(SOUTH_WALL, blockState.getValue(NORTH_WALL));
			case FRONT_BACK:
				return blockState.setValue(EAST_WALL, blockState.getValue(WEST_WALL)).setValue(WEST_WALL, blockState.getValue(EAST_WALL));
			default:
				return super.mirror(blockState, mirror);
		}
	}
}
