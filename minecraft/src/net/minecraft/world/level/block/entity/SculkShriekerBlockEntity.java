package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int WARNING_SOUND_RADIUS = 10;
	private static final int WARDEN_SPAWN_ATTEMPTS = 20;
	private static final int WARDEN_SPAWN_RANGE_XZ = 5;
	private static final int WARDEN_SPAWN_RANGE_Y = 6;
	private static final int DARKNESS_RADIUS = 40;
	private static final int SHRIEKING_TICKS = 90;
	private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
		int2ObjectOpenHashMap.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
		int2ObjectOpenHashMap.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
		int2ObjectOpenHashMap.put(4, SoundEvents.WARDEN_LISTENING_ANGRY);
	});
	private int warningLevel;
	private final VibrationSystem.User vibrationUser = new SculkShriekerBlockEntity.VibrationUser();
	private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
	private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

	public SculkShriekerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_SHRIEKER, blockPos, blockState);
	}

	@Override
	public VibrationSystem.Data getVibrationData() {
		return this.vibrationData;
	}

	@Override
	public VibrationSystem.User getVibrationUser() {
		return this.vibrationUser;
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		if (compoundTag.contains("warning_level", 99)) {
			this.warningLevel = compoundTag.getInt("warning_level");
		}

		if (compoundTag.contains("listener", 10)) {
			VibrationSystem.Data.CODEC
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getCompound("listener")))
				.resultOrPartial(LOGGER::error)
				.ifPresent(data -> this.vibrationData = data);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putInt("warning_level", this.warningLevel);
		VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("listener", tag));
	}

	@Nullable
	public static ServerPlayer tryGetPlayer(@Nullable Entity entity) {
		if (entity instanceof ServerPlayer) {
			return (ServerPlayer)entity;
		} else {
			if (entity != null) {
				LivingEntity serverPlayer2 = entity.getControllingPassenger();
				if (serverPlayer2 instanceof ServerPlayer) {
					return (ServerPlayer)serverPlayer2;
				}
			}

			if (entity instanceof Projectile projectile) {
				Entity var3 = projectile.getOwner();
				if (var3 instanceof ServerPlayer) {
					return (ServerPlayer)var3;
				}
			}

			if (entity instanceof ItemEntity itemEntity) {
				Entity var9 = itemEntity.getOwner();
				if (var9 instanceof ServerPlayer) {
					return (ServerPlayer)var9;
				}
			}

			return null;
		}
	}

	public void tryShriek(ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer) {
		if (serverPlayer != null) {
			BlockState blockState = this.getBlockState();
			if (!(Boolean)blockState.getValue(SculkShriekerBlock.SHRIEKING)) {
				this.warningLevel = 0;
				if (!this.canRespond(serverLevel) || this.tryToWarn(serverLevel, serverPlayer)) {
					this.shriek(serverLevel, serverPlayer);
				}
			}
		}
	}

	private boolean tryToWarn(ServerLevel serverLevel, ServerPlayer serverPlayer) {
		OptionalInt optionalInt = WardenSpawnTracker.tryWarn(serverLevel, this.getBlockPos(), serverPlayer);
		optionalInt.ifPresent(i -> this.warningLevel = i);
		return optionalInt.isPresent();
	}

	private void shriek(ServerLevel serverLevel, @Nullable Entity entity) {
		BlockPos blockPos = this.getBlockPos();
		BlockState blockState = this.getBlockState();
		serverLevel.setBlock(blockPos, blockState.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
		serverLevel.scheduleTick(blockPos, blockState.getBlock(), 90);
		serverLevel.levelEvent(3007, blockPos, 0);
		serverLevel.gameEvent(GameEvent.SHRIEK, blockPos, GameEvent.Context.of(entity));
	}

	private boolean canRespond(ServerLevel serverLevel) {
		return (Boolean)this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON)
			&& serverLevel.getDifficulty() != Difficulty.PEACEFUL
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
	}

	public void tryRespond(ServerLevel serverLevel) {
		if (this.canRespond(serverLevel) && this.warningLevel > 0) {
			if (!this.trySummonWarden(serverLevel)) {
				this.playWardenReplySound(serverLevel);
			}

			Warden.applyDarknessAround(serverLevel, Vec3.atCenterOf(this.getBlockPos()), null, 40);
		}
	}

	private void playWardenReplySound(Level level) {
		SoundEvent soundEvent = SOUND_BY_LEVEL.get(this.warningLevel);
		if (soundEvent != null) {
			BlockPos blockPos = this.getBlockPos();
			int i = blockPos.getX() + Mth.randomBetweenInclusive(level.random, -10, 10);
			int j = blockPos.getY() + Mth.randomBetweenInclusive(level.random, -10, 10);
			int k = blockPos.getZ() + Mth.randomBetweenInclusive(level.random, -10, 10);
			level.playSound(null, (double)i, (double)j, (double)k, soundEvent, SoundSource.HOSTILE, 5.0F, 1.0F);
		}
	}

	private boolean trySummonWarden(ServerLevel serverLevel) {
		return this.warningLevel < 4
			? false
			: SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, serverLevel, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER)
				.isPresent();
	}

	public VibrationSystem.Listener getListener() {
		return this.vibrationListener;
	}

	class VibrationUser implements VibrationSystem.User {
		private static final int LISTENER_RADIUS = 8;
		private final PositionSource positionSource = new BlockPositionSource(SculkShriekerBlockEntity.this.worldPosition);

		public VibrationUser() {
		}

		@Override
		public int getListenerRadius() {
			return 8;
		}

		@Override
		public PositionSource getPositionSource() {
			return this.positionSource;
		}

		@Override
		public TagKey<GameEvent> getListenableEvents() {
			return GameEventTags.SHRIEKER_CAN_LISTEN;
		}

		@Override
		public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, GameEvent.Context context) {
			return !(Boolean)SculkShriekerBlockEntity.this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING)
				&& SculkShriekerBlockEntity.tryGetPlayer(context.sourceEntity()) != null;
		}

		@Override
		public void onReceiveVibration(
			ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity2, float f
		) {
			SculkShriekerBlockEntity.this.tryShriek(serverLevel, SculkShriekerBlockEntity.tryGetPlayer(entity2 != null ? entity2 : entity));
		}

		@Override
		public void onDataChanged() {
			SculkShriekerBlockEntity.this.setChanged();
		}

		@Override
		public boolean requiresAdjacentChunksToBeTicking() {
			return true;
		}
	}
}
