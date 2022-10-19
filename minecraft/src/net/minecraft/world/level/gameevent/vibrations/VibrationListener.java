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
	@VisibleForTesting
	public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps.unmodifiable(
		Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
			object2IntOpenHashMap.put(GameEvent.STEP, 1);
			object2IntOpenHashMap.put(GameEvent.FLAP, 2);
			object2IntOpenHashMap.put(GameEvent.SWIM, 3);
			object2IntOpenHashMap.put(GameEvent.ELYTRA_GLIDE, 4);
			object2IntOpenHashMap.put(GameEvent.HIT_GROUND, 5);
			object2IntOpenHashMap.put(GameEvent.TELEPORT, 5);
			object2IntOpenHashMap.put(GameEvent.SPLASH, 6);
			object2IntOpenHashMap.put(GameEvent.ENTITY_SHAKE, 6);
			object2IntOpenHashMap.put(GameEvent.BLOCK_CHANGE, 6);
			object2IntOpenHashMap.put(GameEvent.NOTE_BLOCK_PLAY, 6);
			object2IntOpenHashMap.put(GameEvent.PROJECTILE_SHOOT, 7);
			object2IntOpenHashMap.put(GameEvent.DRINK, 7);
			object2IntOpenHashMap.put(GameEvent.PRIME_FUSE, 7);
			object2IntOpenHashMap.put(GameEvent.PROJECTILE_LAND, 8);
			object2IntOpenHashMap.put(GameEvent.EAT, 8);
			object2IntOpenHashMap.put(GameEvent.ENTITY_INTERACT, 8);
			object2IntOpenHashMap.put(GameEvent.ENTITY_DAMAGE, 8);
			object2IntOpenHashMap.put(GameEvent.EQUIP, 9);
			object2IntOpenHashMap.put(GameEvent.SHEAR, 9);
			object2IntOpenHashMap.put(GameEvent.ENTITY_ROAR, 9);
			object2IntOpenHashMap.put(GameEvent.BLOCK_CLOSE, 10);
			object2IntOpenHashMap.put(GameEvent.BLOCK_DEACTIVATE, 10);
			object2IntOpenHashMap.put(GameEvent.BLOCK_DETACH, 10);
			object2IntOpenHashMap.put(GameEvent.DISPENSE_FAIL, 10);
			object2IntOpenHashMap.put(GameEvent.BLOCK_OPEN, 11);
			object2IntOpenHashMap.put(GameEvent.BLOCK_ACTIVATE, 11);
			object2IntOpenHashMap.put(GameEvent.BLOCK_ATTACH, 11);
			object2IntOpenHashMap.put(GameEvent.ENTITY_PLACE, 12);
			object2IntOpenHashMap.put(GameEvent.BLOCK_PLACE, 12);
			object2IntOpenHashMap.put(GameEvent.FLUID_PLACE, 12);
			object2IntOpenHashMap.put(GameEvent.ENTITY_DIE, 13);
			object2IntOpenHashMap.put(GameEvent.BLOCK_DESTROY, 13);
			object2IntOpenHashMap.put(GameEvent.FLUID_PICKUP, 13);
			object2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_FINISH, 14);
			object2IntOpenHashMap.put(GameEvent.CONTAINER_CLOSE, 14);
			object2IntOpenHashMap.put(GameEvent.PISTON_CONTRACT, 14);
			object2IntOpenHashMap.put(GameEvent.PISTON_EXTEND, 15);
			object2IntOpenHashMap.put(GameEvent.CONTAINER_OPEN, 15);
			object2IntOpenHashMap.put(GameEvent.EXPLODE, 15);
			object2IntOpenHashMap.put(GameEvent.LIGHTNING_STRIKE, 15);
			object2IntOpenHashMap.put(GameEvent.INSTRUMENT_PLAY, 15);
		})
	);
	protected final PositionSource listenerSource;
	protected final int listenerRange;
	protected final VibrationListener.VibrationListenerConfig config;
	@Nullable
	protected VibrationInfo currentVibration;
	protected int travelTimeInTicks;
	private final VibrationSelector selectionStrategy;

	public static Codec<VibrationListener> codec(VibrationListener.VibrationListenerConfig vibrationListenerConfig) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						PositionSource.CODEC.fieldOf("source").forGetter(vibrationListener -> vibrationListener.listenerSource),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter(vibrationListener -> vibrationListener.listenerRange),
						VibrationInfo.CODEC.optionalFieldOf("event").forGetter(vibrationListener -> Optional.ofNullable(vibrationListener.currentVibration)),
						VibrationSelector.CODEC.fieldOf("selector").forGetter(vibrationListener -> vibrationListener.selectionStrategy),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(vibrationListener -> vibrationListener.travelTimeInTicks)
					)
					.apply(
						instance,
						(positionSource, integer, optional, vibrationSelector, integer2) -> new VibrationListener(
								positionSource, integer, vibrationListenerConfig, (VibrationInfo)optional.orElse(null), vibrationSelector, integer2
							)
					)
		);
	}

	private VibrationListener(
		PositionSource positionSource,
		int i,
		VibrationListener.VibrationListenerConfig vibrationListenerConfig,
		@Nullable VibrationInfo vibrationInfo,
		VibrationSelector vibrationSelector,
		int j
	) {
		this.listenerSource = positionSource;
		this.listenerRange = i;
		this.config = vibrationListenerConfig;
		this.currentVibration = vibrationInfo;
		this.travelTimeInTicks = j;
		this.selectionStrategy = vibrationSelector;
	}

	public VibrationListener(PositionSource positionSource, int i, VibrationListener.VibrationListenerConfig vibrationListenerConfig) {
		this(positionSource, i, vibrationListenerConfig, null, new VibrationSelector(), 0);
	}

	public static int getGameEventFrequency(GameEvent gameEvent) {
		return VIBRATION_FREQUENCY_FOR_EVENT.getOrDefault(gameEvent, 0);
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
					this.config
						.onSignalReceive(
							serverLevel,
							this,
							new BlockPos(this.currentVibration.pos()),
							this.currentVibration.gameEvent(),
							(Entity)this.currentVibration.getEntity(serverLevel).orElse(null),
							(Entity)this.currentVibration.getProjectileOwner(serverLevel).orElse(null),
							this.currentVibration.distance()
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
		return this.listenerRange;
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
				if (!this.config.shouldListen(serverLevel, this, new BlockPos(vec3), gameEvent, context)) {
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

	public interface VibrationListenerConfig {
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
