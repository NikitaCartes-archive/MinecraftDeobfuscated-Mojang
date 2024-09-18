package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<SculkSensorBlock> CODEC = simpleCodec(SculkSensorBlock::new);
	public static final int ACTIVE_TICKS = 30;
	public static final int COOLDOWN_TICKS = 10;
	public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	private static final float[] RESONANCE_PITCH_BEND = Util.make(new float[16], fs -> {
		int[] is = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

		for (int i = 0; i < 16; i++) {
			fs[i] = NoteBlock.getPitchFromNote(is[i]);
		}
	});

	@Override
	public MapCodec<? extends SculkSensorBlock> codec() {
		return CODEC;
	}

	public SculkSensorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
		return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (getPhase(blockState) != SculkSensorPhase.ACTIVE) {
			if (getPhase(blockState) == SculkSensorPhase.COOLDOWN) {
				serverLevel.setBlock(blockPos, blockState.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
				if (!(Boolean)blockState.getValue(WATERLOGGED)) {
					serverLevel.playSound(null, blockPos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, serverLevel.random.nextFloat() * 0.2F + 0.8F);
				}
			}
		} else {
			deactivate(serverLevel, blockPos, blockState);
		}
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (!level.isClientSide()
			&& canActivate(blockState)
			&& entity.getType() != EntityType.WARDEN
			&& level.getBlockEntity(blockPos) instanceof SculkSensorBlockEntity sculkSensorBlockEntity
			&& level instanceof ServerLevel serverLevel
			&& sculkSensorBlockEntity.getVibrationUser().canReceiveVibration(serverLevel, blockPos, GameEvent.STEP, GameEvent.Context.of(blockState))) {
			sculkSensorBlockEntity.getListener().forceScheduleVibration(serverLevel, GameEvent.STEP, GameEvent.Context.of(entity), entity.position());
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!level.isClientSide() && !blockState.is(blockState2.getBlock())) {
			if ((Integer)blockState.getValue(POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
				level.setBlock(blockPos, blockState.setValue(POWER, Integer.valueOf(0)), 18);
			}
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (getPhase(blockState) == SculkSensorPhase.ACTIVE) {
				updateNeighbours(level, blockPos, blockState);
			}
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
		}

		return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	private static void updateNeighbours(Level level, BlockPos blockPos, BlockState blockState) {
		Block block = blockState.getBlock();
		level.updateNeighborsAt(blockPos, block);
		level.updateNeighborsAt(blockPos.below(), block);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SculkSensorBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return !level.isClientSide
			? createTickerHelper(
				blockEntityType,
				BlockEntityType.SCULK_SENSOR,
				(levelx, blockPos, blockStatex, sculkSensorBlockEntity) -> VibrationSystem.Ticker.tick(
						levelx, sculkSensorBlockEntity.getVibrationData(), sculkSensorBlockEntity.getVibrationUser()
					)
			)
			: null;
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return (Integer)blockState.getValue(POWER);
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return direction == Direction.UP ? blockState.getSignal(blockGetter, blockPos, direction) : 0;
	}

	public static SculkSensorPhase getPhase(BlockState blockState) {
		return blockState.getValue(PHASE);
	}

	public static boolean canActivate(BlockState blockState) {
		return getPhase(blockState) == SculkSensorPhase.INACTIVE;
	}

	public static void deactivate(Level level, BlockPos blockPos, BlockState blockState) {
		level.setBlock(blockPos, blockState.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
		level.scheduleTick(blockPos, blockState.getBlock(), 10);
		updateNeighbours(level, blockPos, blockState);
	}

	@VisibleForTesting
	public int getActiveTicks() {
		return 30;
	}

	public void activate(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
		level.setBlock(blockPos, blockState.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(i)), 3);
		level.scheduleTick(blockPos, blockState.getBlock(), this.getActiveTicks());
		updateNeighbours(level, blockPos, blockState);
		tryResonateVibration(entity, level, blockPos, j);
		level.gameEvent(entity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, blockPos);
		if (!(Boolean)blockState.getValue(WATERLOGGED)) {
			level.playSound(
				null,
				(double)blockPos.getX() + 0.5,
				(double)blockPos.getY() + 0.5,
				(double)blockPos.getZ() + 0.5,
				SoundEvents.SCULK_CLICKING,
				SoundSource.BLOCKS,
				1.0F,
				level.random.nextFloat() * 0.2F + 0.8F
			);
		}
	}

	public static void tryResonateVibration(@Nullable Entity entity, Level level, BlockPos blockPos, int i) {
		for (Direction direction : Direction.values()) {
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState = level.getBlockState(blockPos2);
			if (blockState.is(BlockTags.VIBRATION_RESONATORS)) {
				level.gameEvent(VibrationSystem.getResonanceEventByFrequency(i), blockPos2, GameEvent.Context.of(entity, blockState));
				float f = RESONANCE_PITCH_BEND[i];
				level.playSound(null, blockPos2, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, f);
			}
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (getPhase(blockState) == SculkSensorPhase.ACTIVE) {
			Direction direction = Direction.getRandom(randomSource);
			if (direction != Direction.UP && direction != Direction.DOWN) {
				double d = (double)blockPos.getX() + 0.5 + (direction.getStepX() == 0 ? 0.5 - randomSource.nextDouble() : (double)direction.getStepX() * 0.6);
				double e = (double)blockPos.getY() + 0.25;
				double f = (double)blockPos.getZ() + 0.5 + (direction.getStepZ() == 0 ? 0.5 - randomSource.nextDouble() : (double)direction.getStepZ() * 0.6);
				double g = (double)randomSource.nextFloat() * 0.04;
				level.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d, e, f, 0.0, g, 0.0);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PHASE, POWER, WATERLOGGED);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.getBlockEntity(blockPos) instanceof SculkSensorBlockEntity sculkSensorBlockEntity) {
			return getPhase(blockState) == SculkSensorPhase.ACTIVE ? sculkSensorBlockEntity.getLastVibrationFrequency() : 0;
		} else {
			return 0;
		}
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
		if (bl) {
			this.tryDropExperience(serverLevel, blockPos, itemStack, ConstantInt.of(5));
		}
	}
}
