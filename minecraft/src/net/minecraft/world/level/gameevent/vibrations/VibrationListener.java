package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
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
	protected float receivingDistance;
	protected int travelTimeInTicks;

	public static Codec<VibrationListener> codec(VibrationListener.VibrationListenerConfig vibrationListenerConfig) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						PositionSource.CODEC.fieldOf("source").forGetter(vibrationListener -> vibrationListener.listenerSource),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter(vibrationListener -> vibrationListener.listenerRange),
						VibrationListener.ReceivingEvent.CODEC.optionalFieldOf("event").forGetter(vibrationListener -> Optional.ofNullable(vibrationListener.receivingEvent)),
						Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("event_distance").orElse(0.0F).forGetter(vibrationListener -> vibrationListener.receivingDistance),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(vibrationListener -> vibrationListener.travelTimeInTicks)
					)
					.apply(
						instance,
						(positionSource, integer, optional, float_, integer2) -> new VibrationListener(
								positionSource, integer, vibrationListenerConfig, (VibrationListener.ReceivingEvent)optional.orElse(null), float_, integer2
							)
					)
		);
	}

	public VibrationListener(
		PositionSource positionSource,
		int i,
		VibrationListener.VibrationListenerConfig vibrationListenerConfig,
		@Nullable VibrationListener.ReceivingEvent receivingEvent,
		float f,
		int j
	) {
		this.listenerSource = positionSource;
		this.listenerRange = i;
		this.config = vibrationListenerConfig;
		this.receivingEvent = receivingEvent;
		this.receivingDistance = f;
		this.travelTimeInTicks = j;
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
						(Entity)this.receivingEvent.getProjectileOwner(serverLevel).orElse(null),
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
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent.Message message) {
		if (this.receivingEvent != null) {
			return false;
		} else {
			GameEvent gameEvent = message.gameEvent();
			GameEvent.Context context = message.context();
			if (!this.config.isValidVibration(gameEvent, context)) {
				return false;
			} else {
				Optional<Vec3> optional = this.listenerSource.getPosition(serverLevel);
				if (optional.isEmpty()) {
					return false;
				} else {
					Vec3 vec3 = message.source();
					Vec3 vec32 = (Vec3)optional.get();
					if (!this.config.shouldListen(serverLevel, this, new BlockPos(vec3), gameEvent, context)) {
						return false;
					} else if (isOccluded(serverLevel, vec3, vec32)) {
						return false;
					} else {
						this.scheduleSignal(serverLevel, gameEvent, context, vec3, vec32);
						return true;
					}
				}
			}
		}
	}

	private void scheduleSignal(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3, Vec3 vec32) {
		this.receivingDistance = (float)vec3.distanceTo(vec32);
		this.receivingEvent = new VibrationListener.ReceivingEvent(gameEvent, this.receivingDistance, vec3, context.sourceEntity());
		this.travelTimeInTicks = Mth.floor(this.receivingDistance);
		serverLevel.sendParticles(new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
		this.config.onSignalSchedule();
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

	public static record ReceivingEvent(
		GameEvent gameEvent, float distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity
	) {
		public static final Codec<VibrationListener.ReceivingEvent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Registry.GAME_EVENT.byNameCodec().fieldOf("game_event").forGetter(VibrationListener.ReceivingEvent::gameEvent),
						Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationListener.ReceivingEvent::distance),
						Vec3.CODEC.fieldOf("pos").forGetter(VibrationListener.ReceivingEvent::pos),
						ExtraCodecs.UUID.optionalFieldOf("source").forGetter(receivingEvent -> Optional.ofNullable(receivingEvent.uuid())),
						ExtraCodecs.UUID.optionalFieldOf("projectile_owner").forGetter(receivingEvent -> Optional.ofNullable(receivingEvent.projectileOwnerUuid()))
					)
					.apply(
						instance,
						(gameEvent, float_, vec3, optional, optional2) -> new VibrationListener.ReceivingEvent(
								gameEvent, float_, vec3, (UUID)optional.orElse(null), (UUID)optional2.orElse(null)
							)
					)
		);

		public ReceivingEvent(GameEvent gameEvent, float f, Vec3 vec3, @Nullable UUID uUID, @Nullable UUID uUID2) {
			this(gameEvent, f, vec3, uUID, uUID2, null);
		}

		public ReceivingEvent(GameEvent gameEvent, float f, Vec3 vec3, @Nullable Entity entity) {
			this(gameEvent, f, vec3, entity == null ? null : entity.getUUID(), getProjectileOwner(entity), entity);
		}

		@Nullable
		private static UUID getProjectileOwner(@Nullable Entity entity) {
			if (entity instanceof Projectile projectile && projectile.getOwner() != null) {
				return projectile.getOwner().getUUID();
			}

			return null;
		}

		public Optional<Entity> getEntity(ServerLevel serverLevel) {
			return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(serverLevel::getEntity));
		}

		public Optional<Entity> getProjectileOwner(ServerLevel serverLevel) {
			return this.getEntity(serverLevel)
				.filter(entity -> entity instanceof Projectile)
				.map(entity -> (Projectile)entity)
				.map(Projectile::getOwner)
				.or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(serverLevel::getEntity));
		}
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
