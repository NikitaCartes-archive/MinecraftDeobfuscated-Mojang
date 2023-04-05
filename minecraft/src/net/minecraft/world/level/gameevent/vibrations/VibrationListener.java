package net.minecraft.world.level.gameevent.vibrations;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
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
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VibrationListener implements GameEventListener {
	public static final GameEvent[] RESONANCE_EVENTS = new GameEvent[]{
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
	@VisibleForTesting
	public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps.unmodifiable(
		Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
			object2IntOpenHashMap.put(GameEvent.STEP, 1);
			object2IntOpenHashMap.put(GameEvent.SWIM, 1);
			object2IntOpenHashMap.put(GameEvent.FLAP, 1);
			object2IntOpenHashMap.put(GameEvent.PROJECTILE_LAND, 2);
			object2IntOpenHashMap.put(GameEvent.HIT_GROUND, 2);
			object2IntOpenHashMap.put(GameEvent.SPLASH, 2);
			object2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_FINISH, 3);
			object2IntOpenHashMap.put(GameEvent.PROJECTILE_SHOOT, 3);
			object2IntOpenHashMap.put(GameEvent.INSTRUMENT_PLAY, 3);
			object2IntOpenHashMap.put(GameEvent.ENTITY_ROAR, 4);
			object2IntOpenHashMap.put(GameEvent.ENTITY_SHAKE, 4);
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
		})
	);
	private final PositionSource listenerSource;
	private final VibrationListener.Config config;
	@Nullable
	private VibrationInfo currentVibration;
	private int travelTimeInTicks;
	private final VibrationSelector selectionStrategy;

	public static Codec<VibrationListener> codec(VibrationListener.Config config) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						PositionSource.CODEC.fieldOf("source").forGetter(vibrationListener -> vibrationListener.listenerSource),
						VibrationInfo.CODEC.optionalFieldOf("event").forGetter(vibrationListener -> Optional.ofNullable(vibrationListener.currentVibration)),
						VibrationSelector.CODEC.fieldOf("selector").forGetter(vibrationListener -> vibrationListener.selectionStrategy),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(vibrationListener -> vibrationListener.travelTimeInTicks)
					)
					.apply(
						instance,
						(positionSource, optional, vibrationSelector, integer) -> new VibrationListener(
								positionSource, config, (VibrationInfo)optional.orElse(null), vibrationSelector, integer
							)
					)
		);
	}

	private VibrationListener(
		PositionSource positionSource, VibrationListener.Config config, @Nullable VibrationInfo vibrationInfo, VibrationSelector vibrationSelector, int i
	) {
		this.listenerSource = positionSource;
		this.config = config;
		this.currentVibration = vibrationInfo;
		this.travelTimeInTicks = i;
		this.selectionStrategy = vibrationSelector;
	}

	public VibrationListener(PositionSource positionSource, VibrationListener.Config config) {
		this(positionSource, config, null, new VibrationSelector(), 0);
	}

	public static int getGameEventFrequency(GameEvent gameEvent) {
		return VIBRATION_FREQUENCY_FOR_EVENT.getOrDefault(gameEvent, 0);
	}

	public static GameEvent getResonanceEventByFrequency(int i) {
		return RESONANCE_EVENTS[i - 1];
	}

	public VibrationListener.Config getConfig() {
		return this.config;
	}

	public void tick(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			if (this.currentVibration == null) {
				this.selectionStrategy.chosenCandidate(serverLevel.getGameTime()).ifPresent(vibrationInfo -> {
					this.currentVibration = vibrationInfo;
					Vec3 vec3 = this.currentVibration.pos();
					this.travelTimeInTicks = Mth.floor(this.currentVibration.distance());
					serverLevel.sendParticles(new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
					this.config.onSignalSchedule();
					this.selectionStrategy.startOver();
				});
			}

			if (this.currentVibration != null) {
				this.travelTimeInTicks--;
				if (this.travelTimeInTicks <= 0) {
					this.travelTimeInTicks = 0;
					BlockPos blockPos = BlockPos.containing(this.currentVibration.pos());
					BlockPos blockPos2 = (BlockPos)this.listenerSource.getPosition(serverLevel).map(BlockPos::containing).orElse(blockPos);
					this.config
						.onSignalReceive(
							serverLevel,
							this,
							blockPos,
							this.currentVibration.gameEvent(),
							(Entity)this.currentVibration.getEntity(serverLevel).orElse(null),
							(Entity)this.currentVibration.getProjectileOwner(serverLevel).orElse(null),
							distanceBetweenInBlocks(blockPos, blockPos2)
						);
					this.currentVibration = null;
				}
			}
		}
	}

	@Override
	public PositionSource getListenerSource() {
		return this.listenerSource;
	}

	@Override
	public int getListenerRadius() {
		return this.config.getListenerRadius();
	}

	@Override
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
		if (this.currentVibration != null) {
			return false;
		} else if (!this.config.isValidVibration(gameEvent, context)) {
			return false;
		} else {
			Optional<Vec3> optional = this.listenerSource.getPosition(serverLevel);
			if (optional.isEmpty()) {
				return false;
			} else {
				Vec3 vec32 = (Vec3)optional.get();
				if (!this.config.shouldListen(serverLevel, this, BlockPos.containing(vec3), gameEvent, context)) {
					return false;
				} else if (isOccluded(serverLevel, vec3, vec32)) {
					return false;
				} else {
					this.scheduleVibration(serverLevel, gameEvent, context, vec3, vec32);
					return true;
				}
			}
		}
	}

	public void forceGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
		this.listenerSource.getPosition(serverLevel).ifPresent(vec32 -> this.scheduleVibration(serverLevel, gameEvent, context, vec3, vec32));
	}

	public void scheduleVibration(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3, Vec3 vec32) {
		this.selectionStrategy.addCandidate(new VibrationInfo(gameEvent, (float)vec3.distanceTo(vec32), vec3, context.sourceEntity()), serverLevel.getGameTime());
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

	public interface Config {
		int getListenerRadius();

		default TagKey<GameEvent> getListenableEvents() {
			return GameEventTags.VIBRATIONS;
		}

		default boolean canTriggerAvoidVibration() {
			return false;
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

		boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context);

		void onSignalReceive(
			ServerLevel serverLevel,
			GameEventListener gameEventListener,
			BlockPos blockPos,
			GameEvent gameEvent,
			@Nullable Entity entity,
			@Nullable Entity entity2,
			float f
		);

		default void onSignalSchedule() {
		}
	}
}
