package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock extends BaseEntityBlock {
	protected static final VoxelShape OCCLUDER = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	public static final int SHRIEKING_TICKS = 90;
	public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;

	public SculkShriekerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SHRIEKING, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SHRIEKING);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (entity instanceof Player && level instanceof ServerLevel && entity.getType() != EntityType.WARDEN) {
			shriek((ServerLevel)level, level.getBlockState(blockPos), blockPos);
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(SHRIEKING)) {
			serverLevel.setBlock(blockPos, blockState.setValue(SHRIEKING, Boolean.valueOf(false)), 3);
			getPrioritySpawnTracker(serverLevel, blockPos).ifPresent(wardenSpawnTracker -> wardenSpawnTracker.triggerWarningEvent(serverLevel, blockPos));
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos);
		if (bl2 && !level.isClientSide()) {
			ServerLevel serverLevel = (ServerLevel)level;
			shriek(serverLevel, blockState, blockPos);
		}
	}

	public static boolean canShriek(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
		return !(Boolean)blockState.getValue(SHRIEKING)
			&& (Boolean)getPrioritySpawnTracker(serverLevel, blockPos)
				.map(wardenSpawnTracker -> wardenSpawnTracker.canPrepareWarningEvent(serverLevel, blockPos))
				.orElse(false);
	}

	public static void shriek(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos) {
		getPrioritySpawnTracker(serverLevel, blockPos)
			.ifPresent(
				wardenSpawnTracker -> {
					if (wardenSpawnTracker.prepareWarningEvent(serverLevel, blockPos, getPlayersToWarn(serverLevel, blockPos))) {
						serverLevel.scheduleTick(blockPos, blockState.getBlock(), 90);
						serverLevel.setBlock(blockPos, blockState.setValue(SHRIEKING, Boolean.valueOf(true)), 2);
						float f = 2.0F;
						serverLevel.playSound(
							null,
							(double)blockPos.getX(),
							(double)blockPos.getY(),
							(double)blockPos.getZ(),
							SoundEvents.SCULK_SHRIEKER_SHRIEK,
							SoundSource.BLOCKS,
							2.0F,
							0.6F + serverLevel.random.nextFloat() * 0.4F
						);
						serverLevel.gameEvent(null, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, blockPos);

						for (int i = 0; i < 10; i++) {
							int j = i * 5;
							serverLevel.sendParticles(
								new ShriekParticleOption(j), (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0
							);
						}
					}
				}
			);
	}

	private static List<ServerPlayer> getPlayersToWarn(ServerLevel serverLevel, BlockPos blockPos) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		double d = 16.0;
		return serverLevel.getPlayers(serverPlayer -> vec3.distanceToSqr(serverPlayer.position()) < 256.0);
	}

	private static Optional<WardenSpawnTracker> getPrioritySpawnTracker(ServerLevel serverLevel, BlockPos blockPos) {
		Player player = serverLevel.getNearestPlayer((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 16.0, EntitySelector.NO_SPECTATORS);
		return player != null ? Optional.of(player.getWardenSpawnTracker()) : Optional.empty();
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return OCCLUDER;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return OCCLUDER;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SculkShriekerBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getListener(Level level, T blockEntity) {
		return blockEntity instanceof SculkShriekerBlockEntity ? ((SculkShriekerBlockEntity)blockEntity).getListener() : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return !level.isClientSide
			? createTickerHelper(
				blockEntityType,
				BlockEntityType.SCULK_SHRIEKER,
				(levelx, blockPos, blockStatex, sculkShriekerBlockEntity) -> sculkShriekerBlockEntity.getListener().tick(levelx)
			)
			: null;
	}
}
