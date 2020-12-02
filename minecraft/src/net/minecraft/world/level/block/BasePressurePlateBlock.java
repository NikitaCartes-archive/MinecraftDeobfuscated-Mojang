package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock extends Block {
	protected static final VoxelShape PRESSED_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);
	protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
	protected static final AABB TOUCH_AABB = new AABB(0.125, 0.0, 0.125, 0.875, 0.25, 0.875);

	protected BasePressurePlateBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getSignalForState(blockState) > 0 ? PRESSED_AABB : AABB;
	}

	protected int getPressedTime() {
		return 20;
	}

	@Override
	public boolean isPossibleToRespawnInThis() {
		return true;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		return canSupportRigidBlock(levelReader, blockPos2) || canSupportCenter(levelReader, blockPos2, Direction.UP);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		int i = this.getSignalForState(blockState);
		if (i > 0) {
			this.checkPressed(null, serverLevel, blockPos, blockState, i);
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide) {
			int i = this.getSignalForState(blockState);
			if (i == 0) {
				this.checkPressed(entity, level, blockPos, blockState, i);
			}
		}
	}

	protected void checkPressed(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, int i) {
		int j = this.getSignalStrength(level, blockPos);
		boolean bl = i > 0;
		boolean bl2 = j > 0;
		if (i != j) {
			BlockState blockState2 = this.setSignalForState(blockState, j);
			level.setBlock(blockPos, blockState2, 2);
			this.updateNeighbours(level, blockPos);
			level.setBlocksDirty(blockPos, blockState, blockState2);
		}

		if (!bl2 && bl) {
			this.playOffSound(level, blockPos);
			level.gameEvent(entity, GameEvent.BLOCK_UNPRESS, blockPos);
		} else if (bl2 && !bl) {
			this.playOnSound(level, blockPos);
			level.gameEvent(entity, GameEvent.BLOCK_PRESS, blockPos);
		}

		if (bl2) {
			level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, this.getPressedTime());
		}
	}

	protected abstract void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos);

	protected abstract void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos);

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			if (this.getSignalForState(blockState) > 0) {
				this.updateNeighbours(level, blockPos);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	protected void updateNeighbours(Level level, BlockPos blockPos) {
		level.updateNeighborsAt(blockPos, this);
		level.updateNeighborsAt(blockPos.below(), this);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.getSignalForState(blockState);
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return direction == Direction.UP ? this.getSignalForState(blockState) : 0;
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	protected abstract int getSignalStrength(Level level, BlockPos blockPos);

	protected abstract int getSignalForState(BlockState blockState);

	protected abstract BlockState setSignalForState(BlockState blockState, int i);
}
