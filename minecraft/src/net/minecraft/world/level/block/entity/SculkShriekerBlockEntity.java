package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int LISTENER_RADIUS = 8;
	private static final int WARNING_SOUND_RADIUS = 10;
	private static final int WARDEN_SPAWN_ATTEMPTS = 20;
	private static final int WARDEN_SPAWN_RANGE_XZ = 5;
	private static final int WARDEN_SPAWN_RANGE_Y = 6;
	private static final int DARKNESS_RADIUS = 40;
	private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
		int2ObjectOpenHashMap.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
		int2ObjectOpenHashMap.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
	});
	private static final int SHRIEKING_TICKS = 90;
	private int warningLevel;
	private VibrationListener listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this, null, 0, 0);

	public SculkShriekerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_SHRIEKER, blockPos, blockState);
	}

	public VibrationListener getListener() {
		return this.listener;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("warning_level", 99)) {
			this.warningLevel = compoundTag.getInt("warning_level");
		}

		if (compoundTag.contains("listener", 10)) {
			VibrationListener.codec(this)
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getCompound("listener")))
				.resultOrPartial(LOGGER::error)
				.ifPresent(vibrationListener -> this.listener = vibrationListener);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		compoundTag.putInt("warning_level", this.warningLevel);
		VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.listener).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("listener", tag));
	}

	@Override
	public TagKey<GameEvent> getListenableEvents() {
		return GameEventTags.SHRIEKER_CAN_LISTEN;
	}

	@Override
	public boolean shouldListen(
		ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable GameEvent.Context context
	) {
		return !this.isRemoved() && this.canShriek(serverLevel);
	}

	@Override
	public void onSignalReceive(
		ServerLevel serverLevel,
		GameEventListener gameEventListener,
		BlockPos blockPos,
		GameEvent gameEvent,
		@Nullable Entity entity,
		@Nullable Entity entity2,
		int i
	) {
		this.shriek(serverLevel, entity2 != null ? entity2 : entity);
	}

	private boolean canShriek(ServerLevel serverLevel) {
		BlockState blockState = this.getBlockState();
		if ((Boolean)blockState.getValue(SculkShriekerBlock.SHRIEKING)) {
			return false;
		} else if (!(Boolean)blockState.getValue(SculkShriekerBlock.CAN_SUMMON)) {
			return true;
		} else {
			BlockPos blockPos = this.getBlockPos();
			return (Boolean)tryGetSpawnTracker(serverLevel, blockPos).map(wardenSpawnTracker -> wardenSpawnTracker.canWarn(serverLevel, blockPos)).orElse(false);
		}
	}

	public void shriek(ServerLevel serverLevel, @Nullable Entity entity) {
		BlockState blockState = this.getBlockState();
		if (this.canShriek(serverLevel) && this.tryToWarn(serverLevel, blockState)) {
			BlockPos blockPos = this.getBlockPos();
			serverLevel.setBlock(blockPos, blockState.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
			serverLevel.scheduleTick(blockPos, blockState.getBlock(), 90);
			serverLevel.levelEvent(3007, blockPos, 0);
			serverLevel.gameEvent(GameEvent.SHRIEK, blockPos, GameEvent.Context.of(entity));
		}
	}

	private boolean tryToWarn(ServerLevel serverLevel, BlockState blockState) {
		if ((Boolean)blockState.getValue(SculkShriekerBlock.CAN_SUMMON)) {
			BlockPos blockPos = this.getBlockPos();
			Optional<WardenSpawnTracker> optional = tryGetSpawnTracker(serverLevel, blockPos)
				.filter(wardenSpawnTracker -> wardenSpawnTracker.warn(serverLevel, blockPos));
			if (optional.isEmpty()) {
				return false;
			}

			this.warningLevel = ((WardenSpawnTracker)optional.get()).getWarningLevel();
		}

		return true;
	}

	private static Optional<WardenSpawnTracker> tryGetSpawnTracker(ServerLevel serverLevel, BlockPos blockPos) {
		Player player = serverLevel.getNearestPlayer(
			(double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 16.0, EntitySelector.NO_SPECTATORS.and(Entity::isAlive)
		);
		return player == null ? Optional.empty() : Optional.of(player.getWardenSpawnTracker());
	}

	public void replyOrSummon(ServerLevel serverLevel) {
		if ((Boolean)this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON)) {
			Warden.applyDarknessAround(serverLevel, Vec3.atCenterOf(this.getBlockPos()), null, 40);
			if (this.warningLevel >= 3) {
				trySummonWarden(serverLevel, this.getBlockPos());
				return;
			}
		}

		this.playWardenReplySound();
	}

	private void playWardenReplySound() {
		SoundEvent soundEvent = SOUND_BY_LEVEL.get(this.warningLevel);
		if (soundEvent != null) {
			BlockPos blockPos = this.getBlockPos();
			int i = blockPos.getX() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
			int j = blockPos.getY() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
			int k = blockPos.getZ() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
			this.level.playSound(null, (double)i, (double)j, (double)k, soundEvent, SoundSource.HOSTILE, 5.0F, 1.0F);
		}
	}

	private static void trySummonWarden(ServerLevel serverLevel, BlockPos blockPos) {
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING)) {
			SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, serverLevel, blockPos, 20, 5, 6)
				.ifPresent(warden -> warden.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F));
		}
	}

	@Override
	public void onSignalSchedule() {
		this.setChanged();
	}
}
