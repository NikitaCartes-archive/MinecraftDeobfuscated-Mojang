package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block {
	protected static final VoxelShape FLAT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
	protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
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

	public boolean isStraight() {
		return this.isStraight;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		RailShape railShape = blockState.is(this) ? blockState.getValue(this.getShapeProperty()) : null;
		return railShape != null && railShape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return canSupportRigidBlock(levelReader, blockPos.below());
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.updateState(blockState, level, blockPos, bl);
		}
	}

	protected BlockState updateState(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
		blockState = this.updateDir(level, blockPos, blockState, true);
		if (this.isStraight) {
			blockState.neighborChanged(level, blockPos, this, blockPos, bl);
		}

		return blockState;
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			RailShape railShape = blockState.getValue(this.getShapeProperty());
			boolean bl2 = false;
			BlockPos blockPos3 = blockPos.below();
			if (!canSupportRigidBlock(level, blockPos3)) {
				bl2 = true;
			}

			BlockPos blockPos4 = blockPos.east();
			if (railShape == RailShape.ASCENDING_EAST && !canSupportRigidBlock(level, blockPos4)) {
				bl2 = true;
			} else {
				BlockPos blockPos5 = blockPos.west();
				if (railShape == RailShape.ASCENDING_WEST && !canSupportRigidBlock(level, blockPos5)) {
					bl2 = true;
				} else {
					BlockPos blockPos6 = blockPos.north();
					if (railShape == RailShape.ASCENDING_NORTH && !canSupportRigidBlock(level, blockPos6)) {
						bl2 = true;
					} else {
						BlockPos blockPos7 = blockPos.south();
						if (railShape == RailShape.ASCENDING_SOUTH && !canSupportRigidBlock(level, blockPos7)) {
							bl2 = true;
						}
					}
				}
			}

			if (bl2 && !level.isEmptyBlock(blockPos)) {
				if (!bl) {
					dropResources(blockState, level, blockPos);
				}

				level.removeBlock(blockPos, bl);
			} else {
				this.updateState(blockState, level, blockPos, block);
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
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.NORMAL;
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
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
		BlockState blockState = super.defaultBlockState();
		Direction direction = blockPlaceContext.getHorizontalDirection();
		boolean bl = direction == Direction.EAST || direction == Direction.WEST;
		return blockState.setValue(this.getShapeProperty(), bl ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
	}

	public abstract Property<RailShape> getShapeProperty();
}
