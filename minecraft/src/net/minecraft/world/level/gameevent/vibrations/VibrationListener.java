package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
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
	protected final PositionSource listenerSource;
	protected final int listenerRange;
	protected final VibrationListener.VibrationListenerConfig config;
	@Nullable
	protected VibrationListener.ReceivingEvent receivingEvent;
	protected int receivingDistance;
	protected int travelTimeInTicks;

	public static Codec<VibrationListener> codec(VibrationListener.VibrationListenerConfig vibrationListenerConfig) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						PositionSource.CODEC.fieldOf("source").forGetter(vibrationListener -> vibrationListener.listenerSource),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter(vibrationListener -> vibrationListener.listenerRange),
						VibrationListener.ReceivingEvent.CODEC.optionalFieldOf("event").forGetter(vibrationListener -> Optional.ofNullable(vibrationListener.receivingEvent)),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_distance").orElse(0).forGetter(vibrationListener -> vibrationListener.receivingDistance),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(vibrationListener -> vibrationListener.travelTimeInTicks)
					)
					.apply(
						instance,
						(positionSource, integer, optional, integer2, integer3) -> new VibrationListener(
								positionSource, integer, vibrationListenerConfig, (VibrationListener.ReceivingEvent)optional.orElse(null), integer2, integer3
							)
					)
		);
	}

	public VibrationListener(
		PositionSource positionSource,
		int i,
		VibrationListener.VibrationListenerConfig vibrationListenerConfig,
		@Nullable VibrationListener.ReceivingEvent receivingEvent,
		int j,
		int k
	) {
		this.listenerSource = positionSource;
		this.listenerRange = i;
		this.config = vibrationListenerConfig;
		this.receivingEvent = receivingEvent;
		this.receivingDistance = j;
		this.travelTimeInTicks = k;
	}

	public void tick(Level level) {
		if (level instanceof ServerLevel serverLevel && this.receivingEvent != null) {
			this.travelTimeInTicks--;
			if (this.travelTimeInTicks <= 0) {
				this.travelTimeInTicks = 0;
				this.config
					.onSignalReceive(
						serverLevel,
						this,
						new BlockPos(this.receivingEvent.pos),
						this.receivingEvent.gameEvent,
						(Entity)this.receivingEvent.getEntity(serverLevel).orElse(null),
						this.receivingDistance
					);
				this.receivingEvent = null;
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
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3) {
		if (this.receivingEvent != null) {
			return false;
		} else if (!this.config.isValidVibration(gameEvent, entity)) {
			return false;
		} else {
			Optional<Vec3> optional = this.listenerSource.getPosition(serverLevel);
			if (optional.isEmpty()) {
				return false;
			} else {
				Vec3 vec32 = (Vec3)optional.get();
				if (!this.config.shouldListen(serverLevel, this, new BlockPos(vec3), gameEvent, entity)) {
					return false;
				} else if (isOccluded(serverLevel, vec3, vec32)) {
					return false;
				} else {
					this.receiveSignal(serverLevel, gameEvent, entity, vec3, vec32);
					return true;
				}
			}
		}
	}

	private void receiveSignal(ServerLevel serverLevel, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3, Vec3 vec32) {
		this.receivingDistance = Mth.floor(vec3.distanceTo(vec32));
		this.receivingEvent = new VibrationListener.ReceivingEvent(gameEvent, this.receivingDistance, vec3, entity);
		this.travelTimeInTicks = this.receivingDistance;
		serverLevel.sendParticles(new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
	}

	private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {
		Vec3 vec33 = new Vec3((double)Mth.floor(vec3.x) + 0.5, (double)Mth.floor(vec3.y) + 0.5, (double)Mth.floor(vec3.z) + 0.5);
		Vec3 vec34 = new Vec3((double)Mth.floor(vec32.x) + 0.5, (double)Mth.floor(vec32.y) + 0.5, (double)Mth.floor(vec32.z) + 0.5);
		return level.isBlockInLine(new ClipBlockStateContext(vec33, vec34, blockState -> blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType()
			== HitResult.Type.BLOCK;
	}

	public static record ReceivingEvent(GameEvent gameEvent, int distance, Vec3 pos, @Nullable UUID uuid, @Nullable Entity entity) {
		public static final Codec<VibrationListener.ReceivingEvent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Registry.GAME_EVENT.byNameCodec().fieldOf("game_event").forGetter(VibrationListener.ReceivingEvent::gameEvent),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("distance").forGetter(VibrationListener.ReceivingEvent::distance),
						Vec3.CODEC.fieldOf("pos").forGetter(VibrationListener.ReceivingEvent::pos),
						ExtraCodecs.UUID.fieldOf("source").orElse(null).forGetter(VibrationListener.ReceivingEvent::uuid)
					)
					.apply(instance, VibrationListener.ReceivingEvent::new)
		);

		public ReceivingEvent(GameEvent gameEvent, int i, Vec3 vec3, @Nullable UUID uUID) {
			this(gameEvent, i, vec3, uUID, null);
		}

		public ReceivingEvent(GameEvent gameEvent, int i, Vec3 vec3, @Nullable Entity entity) {
			this(gameEvent, i, vec3, entity == null ? null : entity.getUUID(), entity);
		}

		public Optional<Entity> getEntity(ServerLevel serverLevel) {
			return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(serverLevel::getEntity));
		}
	}

	public interface VibrationListenerConfig {
		default TagKey<GameEvent> getListenableEvents() {
			return GameEventTags.VIBRATIONS;
		}

		default boolean isValidVibration(GameEvent gameEvent, @Nullable Entity entity) {
			if (!gameEvent.is(this.getListenableEvents())) {
				return false;
			} else {
				if (entity != null) {
					if (entity.isSpectator()) {
						return false;
					}

					if (entity.isSteppingCarefully() && gameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
						return false;
					}

					if (entity.occludesVibrations()) {
						return false;
					}
				}

				return true;
			}
		}

		boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity);

		void onSignalReceive(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, int i);
	}
}
