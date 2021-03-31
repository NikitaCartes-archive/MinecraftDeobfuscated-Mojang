package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldingBlock extends Block implements SimpleWaterloggedBlock {
	private static final int TICK_DELAY = 1;
	private static final VoxelShape STABLE_SHAPE;
	private static final VoxelShape UNSTABLE_SHAPE;
	private static final VoxelShape UNSTABLE_SHAPE_BOTTOM = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
	private static final VoxelShape BELOW_BLOCK = Shapes.block().move(0.0, -1.0, 0.0);
	public static final int STABILITY_MAX_DISTANCE = 7;
	public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;

	protected ScaffoldingBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(BOTTOM, Boolean.valueOf(false))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DISTANCE, WATERLOGGED, BOTTOM);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if (!collisionContext.isHoldingItem(blockState.getBlock().asItem())) {
			return blockState.getValue(BOTTOM) ? UNSTABLE_SHAPE : STABLE_SHAPE;
		} else {
			return Shapes.block();
		}
	}

	@Override
	public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.block();
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return blockPlaceContext.getItemInHand().is(this.asItem());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Level level = blockPlaceContext.getLevel();
		int i = getDistance(level, blockPos);
		return this.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(level.getFluidState(blockPos).getType() == Fluids.WATER))
			.setValue(DISTANCE, Integer.valueOf(i))
			.setValue(BOTTOM, Boolean.valueOf(this.isBottom(level, blockPos, i)));
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!level.isClientSide) {
			level.getBlockTicks().scheduleTick(blockPos, this, 1);
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		if (!levelAccessor.isClientSide()) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		return blockState;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		int i = getDistance(serverLevel, blockPos);
		BlockState blockState2 = blockState.setValue(DISTANCE, Integer.valueOf(i)).setValue(BOTTOM, Boolean.valueOf(this.isBottom(serverLevel, blockPos, i)));
		if ((Integer)blockState2.getValue(DISTANCE) == 7) {
			if ((Integer)blockState.getValue(DISTANCE) == 7) {
				serverLevel.addFreshEntity(
					new FallingBlockEntity(
						serverLevel,
						(double)blockPos.getX() + 0.5,
						(double)blockPos.getY(),
						(double)blockPos.getZ() + 0.5,
						blockState2.setValue(WATERLOGGED, Boolean.valueOf(false))
					)
				);
			} else {
				serverLevel.destroyBlock(blockPos, true);
			}
		} else if (blockState != blockState2) {
			serverLevel.setBlock(blockPos, blockState2, 3);
		}
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return getDistance(levelReader, blockPos) < 7;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if (collisionContext.isAbove(Shapes.block(), blockPos, true) && !collisionContext.isDescending()) {
			return STABLE_SHAPE;
		} else {
			return blockState.getValue(DISTANCE) != 0 && blockState.getValue(BOTTOM) && collisionContext.isAbove(BELOW_BLOCK, blockPos, true)
				? UNSTABLE_SHAPE_BOTTOM
				: Shapes.empty();
		}
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	private boolean isBottom(BlockGetter blockGetter, BlockPos blockPos, int i) {
		return i > 0 && !blockGetter.getBlockState(blockPos.below()).is(this);
	}

	public static int getDistance(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
		BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
		int i = 7;
		if (blockState.is(Blocks.SCAFFOLDING)) {
			i = (Integer)blockState.getValue(DISTANCE);
		} else if (blockState.isFaceSturdy(blockGetter, mutableBlockPos, Direction.UP)) {
			return 0;
		}

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockState blockState2 = blockGetter.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction));
			if (blockState2.is(Blocks.SCAFFOLDING)) {
				i = Math.min(i, (Integer)blockState2.getValue(DISTANCE) + 1);
				if (i == 1) {
					break;
				}
			}
		}

		return i;
	}

	static {
		VoxelShape voxelShape = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
		VoxelShape voxelShape2 = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0);
		VoxelShape voxelShape3 = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0);
		VoxelShape voxelShape4 = Block.box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0);
		VoxelShape voxelShape5 = Block.box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0);
		STABLE_SHAPE = Shapes.or(voxelShape, voxelShape2, voxelShape3, voxelShape4, voxelShape5);
		VoxelShape voxelShape6 = Block.box(0.0, 0.0, 0.0, 2.0, 2.0, 16.0);
		VoxelShape voxelShape7 = Block.box(14.0, 0.0, 0.0, 16.0, 2.0, 16.0);
		VoxelShape voxelShape8 = Block.box(0.0, 0.0, 14.0, 16.0, 2.0, 16.0);
		VoxelShape voxelShape9 = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 2.0);
		UNSTABLE_SHAPE = Shapes.or(ScaffoldingBlock.UNSTABLE_SHAPE_BOTTOM, STABLE_SHAPE, voxelShape7, voxelShape6, voxelShape9, voxelShape8);
	}
}
