package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBlock extends CrossCollisionBlock {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	private final VoxelShape[] shapeWithPostByIndex;
	private final VoxelShape[] collisionShapeWithPostByIndex;

	public WallBlock(Block.Properties properties) {
		super(0.0F, 3.0F, 0.0F, 14.0F, 24.0F, properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(UP, Boolean.valueOf(true))
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
		this.shapeWithPostByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F);
		this.collisionShapeWithPostByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return blockState.getValue(UP)
			? this.shapeWithPostByIndex[this.getAABBIndex(blockState)]
			: super.getShape(blockState, blockGetter, blockPos, collisionContext);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return blockState.getValue(UP)
			? this.collisionShapeWithPostByIndex[this.getAABBIndex(blockState)]
			: super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	private boolean connectsTo(BlockState blockState, boolean bl, Direction direction) {
		Block block = blockState.getBlock();
		boolean bl2 = block.is(BlockTags.WALLS) || block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(blockState, direction);
		return !isExceptionForConnection(block) && bl || bl2;
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
		BlockState blockState = levelReader.getBlockState(blockPos2);
		BlockState blockState2 = levelReader.getBlockState(blockPos3);
		BlockState blockState3 = levelReader.getBlockState(blockPos4);
		BlockState blockState4 = levelReader.getBlockState(blockPos5);
		boolean bl = this.connectsTo(blockState, blockState.isFaceSturdy(levelReader, blockPos2, Direction.SOUTH), Direction.SOUTH);
		boolean bl2 = this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos3, Direction.WEST), Direction.WEST);
		boolean bl3 = this.connectsTo(blockState3, blockState3.isFaceSturdy(levelReader, blockPos4, Direction.NORTH), Direction.NORTH);
		boolean bl4 = this.connectsTo(blockState4, blockState4.isFaceSturdy(levelReader, blockPos5, Direction.EAST), Direction.EAST);
		boolean bl5 = (!bl || bl2 || !bl3 || bl4) && (bl || !bl2 || bl3 || !bl4);
		return this.defaultBlockState()
			.setValue(UP, Boolean.valueOf(bl5 || !levelReader.isEmptyBlock(blockPos.above())))
			.setValue(NORTH, Boolean.valueOf(bl))
			.setValue(EAST, Boolean.valueOf(bl2))
			.setValue(SOUTH, Boolean.valueOf(bl3))
			.setValue(WEST, Boolean.valueOf(bl4))
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
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
			Direction direction2 = direction.getOpposite();
			boolean bl = direction == Direction.NORTH
				? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2)
				: (Boolean)blockState.getValue(NORTH);
			boolean bl2 = direction == Direction.EAST
				? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2)
				: (Boolean)blockState.getValue(EAST);
			boolean bl3 = direction == Direction.SOUTH
				? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2)
				: (Boolean)blockState.getValue(SOUTH);
			boolean bl4 = direction == Direction.WEST
				? this.connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction2), direction2)
				: (Boolean)blockState.getValue(WEST);
			boolean bl5 = (!bl || bl2 || !bl3 || bl4) && (bl || !bl2 || bl3 || !bl4);
			return blockState.setValue(UP, Boolean.valueOf(bl5 || !levelAccessor.isEmptyBlock(blockPos.above())))
				.setValue(NORTH, Boolean.valueOf(bl))
				.setValue(EAST, Boolean.valueOf(bl2))
				.setValue(SOUTH, Boolean.valueOf(bl3))
				.setValue(WEST, Boolean.valueOf(bl4));
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}
}
