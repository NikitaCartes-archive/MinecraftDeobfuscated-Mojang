package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty CAN_SUMMON = BlockStateProperties.CAN_SUMMON;
	protected static final VoxelShape COLLIDER = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	private static final int SHRIEKING_TICKS = 90;
	public static final double TOP_Y = COLLIDER.max(Direction.Axis.Y);

	public SculkShriekerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(SHRIEKING, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
				.setValue(CAN_SUMMON, Boolean.valueOf(false))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SHRIEKING);
		builder.add(WATERLOGGED);
		builder.add(CAN_SUMMON);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (entity instanceof Player && level instanceof ServerLevel serverLevel) {
			shriek(serverLevel, blockState, blockPos);
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(SHRIEKING)) {
			serverLevel.setBlock(blockPos, blockState.setValue(SHRIEKING, Boolean.valueOf(false)), 3);
			if ((Boolean)blockState.getValue(CAN_SUMMON)) {
				getWardenSpawnTracker(serverLevel, blockPos).ifPresent(wardenSpawnTracker -> wardenSpawnTracker.triggerWarningEvent(serverLevel, blockPos));
			}
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (level instanceof ServerLevel serverLevel && level.hasNeighborSignal(blockPos)) {
			shriek(serverLevel, blockState, blockPos);
		}
	}

	public static boolean canShriek(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
		return !(Boolean)blockState.getValue(SHRIEKING)
			&& (
				!(Boolean)blockState.getValue(CAN_SUMMON)
					|| (Boolean)getWardenSpawnTracker(serverLevel, blockPos)
						.map(wardenSpawnTracker -> wardenSpawnTracker.canPrepareWarningEvent(serverLevel, blockPos))
						.orElse(false)
			);
	}

	public static void shriek(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos) {
		if (canShriek(serverLevel, blockPos, blockState)) {
			if (!(Boolean)blockState.getValue(CAN_SUMMON)
				|| getWardenSpawnTracker(serverLevel, blockPos).filter(wardenSpawnTracker -> wardenSpawnTracker.prepareWarningEvent(serverLevel, blockPos)).isPresent()) {
				serverLevel.setBlock(blockPos, blockState.setValue(SHRIEKING, Boolean.valueOf(true)), 2);
				serverLevel.scheduleTick(blockPos, blockState.getBlock(), 90);
				serverLevel.levelEvent(3007, blockPos, 0);
			}
		}
	}

	private static Optional<WardenSpawnTracker> getWardenSpawnTracker(ServerLevel serverLevel, BlockPos blockPos) {
		Player player = serverLevel.getNearestPlayer(
			(double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 16.0, EntitySelector.NO_SPECTATORS.and(Entity::isAlive)
		);
		return player == null ? Optional.empty() : Optional.of(player.getWardenSpawnTracker());
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return COLLIDER;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return COLLIDER;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SculkShriekerBlockEntity(blockPos, blockState);
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

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
		this.tryDropExperience(serverLevel, blockPos, itemStack, ConstantInt.of(5));
	}

	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T blockEntity) {
		return blockEntity instanceof SculkShriekerBlockEntity sculkShriekerBlockEntity ? sculkShriekerBlockEntity.getListener() : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return !level.isClientSide
			? BaseEntityBlock.createTickerHelper(
				blockEntityType,
				BlockEntityType.SCULK_SHRIEKER,
				(levelx, blockPos, blockStatex, sculkShriekerBlockEntity) -> sculkShriekerBlockEntity.getListener().tick(levelx)
			)
			: null;
	}
}
