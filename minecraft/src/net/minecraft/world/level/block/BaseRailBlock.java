package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock {
	protected static final VoxelShape FLAT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
	protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private final boolean isStraight;

	public static boolean isRail(Level level, BlockPos blockPos) {
		return isRail(level.getBlockState(blockPos));
	}

	public static boolean isRail(BlockState blockState) {
		return blockState.is(BlockTags.RAILS) && blockState.getBlock() instanceof BaseRailBlock;
	}

	protected BaseRailBlock(boolean bl, BlockBehaviour.Properties properties) {
		super(properties);
		this.isStraight = bl;
	}

	@Override
	protected abstract MapCodec<? extends BaseRailBlock> codec();

	public boolean isStraight() {
		return this.isStraight;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		RailShape railShape = blockState.is(this) ? blockState.getValue(this.getShapeProperty()) : null;
		return railShape != null && railShape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return canSupportRigidBlock(levelReader, blockPos.below());
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.updateState(blockState, level, blockPos, bl);
		}
	}

	protected BlockState updateState(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
		blockState = this.updateDir(level, blockPos, blockState, true);
		if (this.isStraight) {
			level.neighborChanged(blockState, blockPos, this, blockPos, bl);
		}

		return blockState;
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide && level.getBlockState(blockPos).is(this)) {
			RailShape railShape = blockState.getValue(this.getShapeProperty());
			if (shouldBeRemoved(blockPos, level, railShape)) {
				dropResources(blockState, level, blockPos);
				level.removeBlock(blockPos, bl);
			} else {
				this.updateState(blockState, level, blockPos, block);
			}
		}
	}

	private static boolean shouldBeRemoved(BlockPos blockPos, Level level, RailShape railShape) {
		if (!canSupportRigidBlock(level, blockPos.below())) {
			return true;
		} else {
			switch (railShape) {
				case ASCENDING_EAST:
					return !canSupportRigidBlock(level, blockPos.east());
				case ASCENDING_WEST:
					return !canSupportRigidBlock(level, blockPos.west());
				case ASCENDING_NORTH:
					return !canSupportRigidBlock(level, blockPos.north());
				case ASCENDING_SOUTH:
					return !canSupportRigidBlock(level, blockPos.south());
				default:
					return false;
			}
		}
	}

	protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
	}

	protected BlockState updateDir(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
		if (level.isClientSide) {
			return blockState;
		} else {
			RailShape railShape = blockState.getValue(this.getShapeProperty());
			return new RailState(level, blockPos, blockState).place(level.hasNeighborSignal(blockPos), bl, railShape).getState();
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl) {
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (((RailShape)blockState.getValue(this.getShapeProperty())).isAscending()) {
				level.updateNeighborsAt(blockPos.above(), this);
			}

			if (this.isStraight) {
				level.updateNeighborsAt(blockPos, this);
				level.updateNeighborsAt(blockPos.below(), this);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		boolean bl = fluidState.getType() == Fluids.WATER;
		BlockState blockState = super.defaultBlockState();
		Direction direction = blockPlaceContext.getHorizontalDirection();
		boolean bl2 = direction == Direction.EAST || direction == Direction.WEST;
		return blockState.setValue(this.getShapeProperty(), bl2 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(bl));
	}

	public abstract Property<RailShape> getShapeProperty();

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}
}
