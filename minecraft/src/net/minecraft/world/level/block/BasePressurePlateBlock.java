package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock extends Block {
	protected static final VoxelShape PRESSED_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);
	protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
	protected static final AABB TOUCH_AABB = new AABB(0.0625, 0.0, 0.0625, 0.9375, 0.25, 0.9375);
	protected final BlockSetType type;

	protected BasePressurePlateBlock(BlockBehaviour.Properties properties, BlockSetType blockSetType) {
		super(properties.sound(blockSetType.soundType()));
		this.type = blockSetType;
	}

	@Override
	protected abstract MapCodec<? extends BasePressurePlateBlock> codec();

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getSignalForState(blockState) > 0 ? PRESSED_AABB : AABB;
	}

	protected int getPressedTime() {
		return 20;
	}

	@Override
	public boolean isPossibleToRespawnInThis(BlockState blockState) {
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
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
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

	private void checkPressed(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, int i) {
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
			level.playSound(null, blockPos, this.type.pressurePlateClickOff(), SoundSource.BLOCKS);
			level.gameEvent(entity, GameEvent.BLOCK_DEACTIVATE, blockPos);
		} else if (bl2 && !bl) {
			level.playSound(null, blockPos, this.type.pressurePlateClickOn(), SoundSource.BLOCKS);
			level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, blockPos);
		}

		if (bl2) {
			level.scheduleTick(new BlockPos(blockPos), this, this.getPressedTime());
		}
	}

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

	protected static int getEntityCount(Level level, AABB aABB, Class<? extends Entity> class_) {
		return level.getEntitiesOfClass(class_, aABB, EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers())).size();
	}

	protected abstract int getSignalStrength(Level level, BlockPos blockPos);

	protected abstract int getSignalForState(BlockState blockState);

	protected abstract BlockState setSignalForState(BlockState blockState, int i);
}
