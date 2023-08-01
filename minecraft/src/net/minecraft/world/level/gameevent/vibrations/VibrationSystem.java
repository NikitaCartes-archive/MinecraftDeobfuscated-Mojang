package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface VibrationSystem {
	GameEvent[] RESONANCE_EVENTS = new GameEvent[]{
		GameEvent.RESONATE_1,
		GameEvent.RESONATE_2,
		GameEvent.RESONATE_3,
		GameEvent.RESONATE_4,
		GameEvent.RESONATE_5,
		GameEvent.RESONATE_6,
		GameEvent.RESONATE_7,
		GameEvent.RESONATE_8,
		GameEvent.RESONATE_9,
		GameEvent.RESONATE_10,
		GameEvent.RESONATE_11,
		GameEvent.RESONATE_12,
		GameEvent.RESONATE_13,
		GameEvent.RESONATE_14,
		GameEvent.RESONATE_15
	};
	ToIntFunction<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
		object2IntOpenHashMap.defaultReturnValue(0);
		object2IntOpenHashMap.put(GameEvent.STEP, 1);
		object2IntOpenHashMap.put(GameEvent.SWIM, 1);
		object2IntOpenHashMap.put(GameEvent.FLAP, 1);
		object2IntOpenHashMap.put(GameEvent.PROJECTILE_LAND, 2);
		object2IntOpenHashMap.put(GameEvent.HIT_GROUND, 2);
		object2IntOpenHashMap.put(GameEvent.SPLASH, 2);
		object2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_FINISH, 3);
		object2IntOpenHashMap.put(GameEvent.PROJECTILE_SHOOT, 3);
		object2IntOpenHashMap.put(GameEvent.INSTRUMENT_PLAY, 3);
		object2IntOpenHashMap.put(GameEvent.ENTITY_ACTION, 4);
		object2IntOpenHashMap.put(GameEvent.ELYTRA_GLIDE, 4);
		object2IntOpenHashMap.put(GameEvent.ENTITY_DISMOUNT, 5);
		object2IntOpenHashMap.put(GameEvent.EQUIP, 5);
		object2IntOpenHashMap.put(GameEvent.ENTITY_INTERACT, 6);
		object2IntOpenHashMap.put(GameEvent.SHEAR, 6);
		object2IntOpenHashMap.put(GameEvent.ENTITY_MOUNT, 6);
		object2IntOpenHashMap.put(GameEvent.ENTITY_DAMAGE, 7);
		object2IntOpenHashMap.put(GameEvent.DRINK, 8);
		object2IntOpenHashMap.put(GameEvent.EAT, 8);
		object2IntOpenHashMap.put(GameEvent.CONTAINER_CLOSE, 9);
		object2IntOpenHashMap.put(GameEvent.BLOCK_CLOSE, 9);
		object2IntOpenHashMap.put(GameEvent.BLOCK_DEACTIVATE, 9);
		object2IntOpenHashMap.put(GameEvent.BLOCK_DETACH, 9);
		object2IntOpenHashMap.put(GameEvent.CONTAINER_OPEN, 10);
		object2IntOpenHashMap.put(GameEvent.BLOCK_OPEN, 10);
		object2IntOpenHashMap.put(GameEvent.BLOCK_ACTIVATE, 10);
		object2IntOpenHashMap.put(GameEvent.BLOCK_ATTACH, 10);
		object2IntOpenHashMap.put(GameEvent.PRIME_FUSE, 10);
		object2IntOpenHashMap.put(GameEvent.NOTE_BLOCK_PLAY, 10);
		object2IntOpenHashMap.put(GameEvent.BLOCK_CHANGE, 11);
		object2IntOpenHashMap.put(GameEvent.BLOCK_DESTROY, 12);
		object2IntOpenHashMap.put(GameEvent.FLUID_PICKUP, 12);
		object2IntOpenHashMap.put(GameEvent.BLOCK_PLACE, 13);
		object2IntOpenHashMap.put(GameEvent.FLUID_PLACE, 13);
		object2IntOpenHashMap.put(GameEvent.ENTITY_PLACE, 14);
		object2IntOpenHashMap.put(GameEvent.LIGHTNING_STRIKE, 14);
		object2IntOpenHashMap.put(GameEvent.TELEPORT, 14);
		object2IntOpenHashMap.put(GameEvent.ENTITY_DIE, 15);
		object2IntOpenHashMap.put(GameEvent.EXPLODE, 15);

		for (int i = 1; i <= 15; i++) {
			object2IntOpenHashMap.put(getResonanceEventByFrequency(i), i);
		}
	});

	VibrationSystem.Data getVibrationData();

	VibrationSystem.User getVibrationUser();

	static int getGameEventFrequency(GameEvent gameEvent) {
		return VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(gameEvent);
	}

	static GameEvent getResonanceEventByFrequency(int i) {
		return RESONANCE_EVENTS[i - 1];
	}

	static int getRedstoneStrengthForDistance(float f, int i) {
		double d = 15.0 / (double)i;
		return Math.max(1, 15 - Mth.floor(d * (double)f));
	}

	public static final class Data {
		public static Codec<VibrationSystem.Data> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						VibrationInfo.CODEC.optionalFieldOf("event").forGetter(data -> Optional.ofNullable(data.currentVibration)),
						VibrationSelector.CODEC.fieldOf("selector").forGetter(VibrationSystem.Data::getSelectionStrategy),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(VibrationSystem.Data::getTravelTimeInTicks)
					)
					.apply(
						instance, (optional, vibrationSelector, integer) -> new VibrationSystem.Data((VibrationInfo)optional.orElse(null), vibrationSelector, integer, true)
					)
		);
		public static final String NBT_TAG_KEY = "listener";
		@Nullable
		VibrationInfo currentVibration;
		private int travelTimeInTicks;
		final VibrationSelector selectionStrategy;
		private boolean reloadVibrationParticle;

		private Data(@Nullable VibrationInfo vibrationInfo, VibrationSelector vibrationSelector, int i, boolean bl) {
			this.currentVibration = vibrationInfo;
			this.travelTimeInTicks = i;
			this.selectionStrategy = vibrationSelector;
			this.reloadVibrationParticle = bl;
		}

		public Data() {
			this(null, new VibrationSelector(), 0, false);
		}

		public VibrationSelector getSelectionStrategy() {
			return this.selectionStrategy;
		}

		@Nullable
		public VibrationInfo getCurrentVibration() {
			return this.currentVibration;
		}

		public void setCurrentVibration(@Nullable VibrationInfo vibrationInfo) {
			this.currentVibration = vibrationInfo;
		}

		public int getTravelTimeInTicks() {
			return this.travelTimeInTicks;
		}

		public void setTravelTimeInTicks(int i) {
			this.travelTimeInTicks = i;
		}

		public void decrementTravelTime() {
			this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
		}

		public boolean shouldReloadVibrationParticle() {
			return this.reloadVibrationParticle;
		}

		public void setReloadVibrationParticle(boolean bl) {
			this.reloadVibrationParticle = bl;
		}
	}

	public static class Listener implements GameEventListener {
		private final VibrationSystem system;

		public Listener(VibrationSystem vibrationSystem) {
			this.system = vibrationSystem;
		}

		@Override
		public PositionSource getListenerSource() {
			return this.system.getVibrationUser().getPositionSource();
		}

		@Override
		public int getListenerRadius() {
			return this.system.getVibrationUser().getListenerRadius();
		}

		@Override
		public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
			VibrationSystem.Data data = this.system.getVibrationData();
			VibrationSystem.User user = this.system.getVibrationUser();
			if (data.getCurrentVibration() != null) {
				return false;
			} else if (!user.isValidVibration(gameEvent, context)) {
				return false;
			} else {
				Optional<Vec3> optional = user.getPositionSource().getPosition(serverLevel);
				if (optional.isEmpty()) {
					return false;
				} else {
					Vec3 vec32 = (Vec3)optional.get();
					if (!user.canReceiveVibration(serverLevel, BlockPos.containing(vec3), gameEvent, context)) {
						return false;
					} else if (isOccluded(serverLevel, vec3, vec32)) {
						return false;
					} else {
						this.scheduleVibration(serverLevel, data, gameEvent, context, vec3, vec32);
						return true;
					}
				}
			}
		}

		public void forceScheduleVibration(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
			this.system
				.getVibrationUser()
				.getPositionSource()
				.getPosition(serverLevel)
				.ifPresent(vec32 -> this.scheduleVibration(serverLevel, this.system.getVibrationData(), gameEvent, context, vec3, vec32));
		}

		private void scheduleVibration(ServerLevel serverLevel, VibrationSystem.Data data, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3, Vec3 vec32) {
			data.selectionStrategy.addCandidate(new VibrationInfo(gameEvent, (float)vec3.distanceTo(vec32), vec3, context.sourceEntity()), serverLevel.getGameTime());
		}

		public static float distanceBetweenInBlocks(BlockPos blockPos, BlockPos blockPos2) {
			return (float)Math.sqrt(blockPos.distSqr(blockPos2));
		}

		private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {
			Vec3 vec33 = new Vec3((double)Mth.floor(vec3.x) + 0.5, (double)Mth.floor(vec3.y) + 0.5, (double)Mth.floor(vec3.z) + 0.5);
			Vec3 vec34 = new Vec3((double)Mth.floor(vec32.x) + 0.5, (double)Mth.floor(vec32.y) + 0.5, (double)Mth.floor(vec32.z) + 0.5);

			for (Direction direction : Direction.values()) {
				Vec3 vec35 = vec33.relative(direction, 1.0E-5F);
				if (level.isBlockInLine(new ClipBlockStateContext(vec35, vec34, blockState -> blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType()
					!= HitResult.Type.BLOCK) {
					return false;
				}
			}

			return true;
		}
	}

	public interface Ticker {
		static void tick(Level level, VibrationSystem.Data data, VibrationSystem.User user) {
			if (level instanceof ServerLevel serverLevel) {
				if (data.currentVibration == null) {
					trySelectAndScheduleVibration(serverLevel, data, user);
				}

				if (data.currentVibration != null) {
					boolean bl = data.getTravelTimeInTicks() > 0;
					tryReloadVibrationParticle(serverLevel, data, user);
					data.decrementTravelTime();
					if (data.getTravelTimeInTicks() <= 0) {
						bl = receiveVibration(serverLevel, data, user, data.currentVibration);
					}

					if (bl) {
						user.onDataChanged();
					}
				}
			}
		}

		private static void trySelectAndScheduleVibration(ServerLevel serverLevel, VibrationSystem.Data data, VibrationSystem.User user) {
			data.getSelectionStrategy()
				.chosenCandidate(serverLevel.getGameTime())
				.ifPresent(
					vibrationInfo -> {
						data.setCurrentVibration(vibrationInfo);
						Vec3 vec3 = vibrationInfo.pos();
						data.setTravelTimeInTicks(user.calculateTravelTimeInTicks(vibrationInfo.distance()));
						serverLevel.sendParticles(
							new VibrationParticleOption(user.getPositionSource(), data.getTravelTimeInTicks()), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0
						);
						user.onDataChanged();
						data.getSelectionStrategy().startOver();
					}
				);
		}

		private static void tryReloadVibrationParticle(ServerLevel serverLevel, VibrationSystem.Data data, VibrationSystem.User user) {
			if (data.shouldReloadVibrationParticle()) {
				if (data.currentVibration == null) {
					data.setReloadVibrationParticle(false);
				} else {
					Vec3 vec3 = data.currentVibration.pos();
					PositionSource positionSource = user.getPositionSource();
					Vec3 vec32 = (Vec3)positionSource.getPosition(serverLevel).orElse(vec3);
					int i = data.getTravelTimeInTicks();
					int j = user.calculateTravelTimeInTicks(data.currentVibration.distance());
					double d = 1.0 - (double)i / (double)j;
					double e = Mth.lerp(d, vec3.x, vec32.x);
					double f = Mth.lerp(d, vec3.y, vec32.y);
					double g = Mth.lerp(d, vec3.z, vec32.z);
					boolean bl = serverLevel.sendParticles(new VibrationParticleOption(positionSource, i), e, f, g, 1, 0.0, 0.0, 0.0, 0.0) > 0;
					if (bl) {
						data.setReloadVibrationParticle(false);
					}
				}
			}
		}

		private static boolean receiveVibration(ServerLevel serverLevel, VibrationSystem.Data data, VibrationSystem.User user, VibrationInfo vibrationInfo) {
			BlockPos blockPos = BlockPos.containing(vibrationInfo.pos());
			BlockPos blockPos2 = (BlockPos)user.getPositionSource().getPosition(serverLevel).map(BlockPos::containing).orElse(blockPos);
			if (user.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking(serverLevel, blockPos2)) {
				return false;
			} else {
				user.onReceiveVibration(
					serverLevel,
					blockPos,
					vibrationInfo.gameEvent(),
					(Entity)vibrationInfo.getEntity(serverLevel).orElse(null),
					(Entity)vibrationInfo.getProjectileOwner(serverLevel).orElse(null),
					VibrationSystem.Listener.distanceBetweenInBlocks(blockPos, blockPos2)
				);
				data.setCurrentVibration(null);
				return true;
			}
		}

		private static boolean areAdjacentChunksTicking(Level level, BlockPos blockPos) {
			ChunkPos chunkPos = new ChunkPos(blockPos);

			for (int i = chunkPos.x - 1; i < chunkPos.x + 1; i++) {
				for (int j = chunkPos.z - 1; j < chunkPos.z + 1; j++) {
					ChunkAccess chunkAccess = level.getChunkSource().getChunkNow(i, j);
					if (chunkAccess == null || !level.shouldTickBlocksAt(chunkAccess.getPos().toLong())) {
						return false;
					}
				}
			}

			return true;
		}
	}

	public interface User {
		int getListenerRadius();

		PositionSource getPositionSource();

		boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context);

		void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity2, float f);

		default TagKey<GameEvent> getListenableEvents() {
			return GameEventTags.VIBRATIONS;
		}

		default boolean canTriggerAvoidVibration() {
			return false;
		}

		default boolean requiresAdjacentChunksToBeTicking() {
			return false;
		}

		default int calculateTravelTimeInTicks(float f) {
			return Mth.floor(f);
		}

		default boolean isValidVibration(GameEvent gameEvent, GameEvent.Context context) {
			if (!gameEvent.is(this.getListenableEvents())) {
				return false;
			} else {
				Entity entity = context.sourceEntity();
				if (entity != null) {
					if (entity.isSpectator()) {
						return false;
					}

					if (entity.isSteppingCarefully() && gameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
						if (this.canTriggerAvoidVibration() && entity instanceof ServerPlayer serverPlayer) {
							CriteriaTriggers.AVOID_VIBRATION.trigger(serverPlayer);
						}

						return false;
					}

					if (entity.dampensVibrations()) {
						return false;
					}
				}

				return context.affectedState() != null ? !context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS) : true;
			}
		}

		default void onDataChanged() {
		}
	}
}
