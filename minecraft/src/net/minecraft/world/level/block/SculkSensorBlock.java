package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final int ACTIVE_TICKS = 40;
	public static final int COOLDOWN_TICKS = 1;
	public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	private final int listenerRange;

	public SculkSensorBlock(BlockBehaviour.Properties properties, int i) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
		this.listenerRange = i;
	}

	public int getListenerRange() {
		return this.listenerRange;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
		return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (getPhase(blockState) != SculkSensorPhase.ACTIVE) {
			if (getPhase(blockState) == SculkSensorPhase.COOLDOWN) {
				serverLevel.setBlock(blockPos, blockState.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
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
			&& level instanceof ServerLevel serverLevel) {
			sculkSensorBlockEntity.getListener().forceGameEvent(serverLevel, GameEvent.STEP, GameEvent.Context.of(entity), entity.position());
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!level.isClientSide() && !blockState.is(blockState2.getBlock())) {
			if ((Integer)blockState.getValue(POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
				level.setBlock(blockPos, blockState.setValue(POWER, Integer.valueOf(0)), 18);
			}

			level.scheduleTick(new BlockPos(blockPos), blockState.getBlock(), 1);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (getPhase(blockState) == SculkSensorPhase.ACTIVE) {
				updateNeighbours(level, blockPos);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	private static void updateNeighbours(Level level, BlockPos blockPos) {
		level.updateNeighborsAt(blockPos, Blocks.SCULK_SENSOR);
		level.updateNeighborsAt(blockPos.relative(Direction.UP.getOpposite()), Blocks.SCULK_SENSOR);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SculkSensorBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T blockEntity) {
		return blockEntity instanceof SculkSensorBlockEntity ? ((SculkSensorBlockEntity)blockEntity).getListener() : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return !level.isClientSide
			? createTickerHelper(
				blockEntityType, BlockEntityType.SCULK_SENSOR, (levelx, blockPos, blockStatex, sculkSensorBlockEntity) -> sculkSensorBlockEntity.getListener().tick(levelx)
			)
			: null;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return (Integer)blockState.getValue(POWER);
	}

	public static SculkSensorPhase getPhase(BlockState blockState) {
		return blockState.getValue(PHASE);
	}

	public static boolean canActivate(BlockState blockState) {
		return getPhase(blockState) == SculkSensorPhase.INACTIVE;
	}

	public static void deactivate(Level level, BlockPos blockPos, BlockState blockState) {
		level.setBlock(blockPos, blockState.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
		level.scheduleTick(blockPos, blockState.getBlock(), 1);
		if (!(Boolean)blockState.getValue(WATERLOGGED)) {
			level.playSound(null, blockPos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);
		}

		updateNeighbours(level, blockPos);
	}

	public static void activate(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, int i) {
		level.setBlock(blockPos, blockState.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(i)), 3);
		level.scheduleTick(blockPos, blockState.getBlock(), 40);
		updateNeighbours(level, blockPos);
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
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.getBlockEntity(blockPos) instanceof SculkSensorBlockEntity sculkSensorBlockEntity) {
			return getPhase(blockState) == SculkSensorPhase.ACTIVE ? sculkSensorBlockEntity.getLastVibrationFrequency() : 0;
		} else {
			return 0;
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
		if (bl) {
			this.tryDropExperience(serverLevel, blockPos, itemStack, ConstantInt.of(5));
		}
	}
}
