package net.minecraft.world.level.gameevent.vibrations;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

	public VibrationListener(PositionSource positionSource, int i, VibrationListener.VibrationListenerConfig vibrationListenerConfig) {
		this.listenerSource = positionSource;
		this.listenerRange = i;
		this.config = vibrationListenerConfig;
	}

	public void tick(Level level) {
		if (this.receivingEvent != null) {
			this.travelTimeInTicks--;
			if (this.travelTimeInTicks <= 0) {
				this.travelTimeInTicks = 0;
				this.config
					.onSignalReceive(
						level, this, new BlockPos(this.receivingEvent.pos), this.receivingEvent.gameEvent, this.receivingEvent.sourceEntity, this.receivingDistance
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
	public boolean handleGameEvent(Level level, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3) {
		if (this.receivingEvent != null) {
			return false;
		} else if (!this.config.isValidVibration(gameEvent, entity)) {
			return false;
		} else {
			Optional<Vec3> optional = this.listenerSource.getPosition(level);
			if (optional.isEmpty()) {
				return false;
			} else {
				Vec3 vec32 = (Vec3)optional.get();
				BlockPos blockPos = new BlockPos(vec3);
				if (!this.config.shouldListen(level, this, blockPos, gameEvent, entity)) {
					return false;
				} else if (this.config.shouldApplyWoolOcclusion(level, this, blockPos, gameEvent, entity) && isOccluded(level, vec3, vec32)) {
					return false;
				} else {
					if (level instanceof ServerLevel serverLevel) {
						this.receiveSignal(serverLevel, gameEvent, entity, vec3, vec32);
					}

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

	public static record ReceivingEvent(GameEvent gameEvent, int distance, Vec3 pos, @Nullable Entity sourceEntity) {
	}

	public interface VibrationListenerConfig {
		default Tag.Named<GameEvent> getListenableEvents() {
			return GameEventTags.VIBRATIONS;
		}

		default boolean isValidVibration(GameEvent gameEvent, @Nullable Entity entity) {
			if (!this.getListenableEvents().contains(gameEvent)) {
				return false;
			} else {
				if (entity != null) {
					if (entity.isSpectator()) {
						return false;
					}

					if (entity.isSteppingCarefully() && GameEventTags.IGNORE_VIBRATIONS_SNEAKING.contains(gameEvent)) {
						return false;
					}

					if (entity.occludesVibrations()) {
						return false;
					}
				}

				return true;
			}
		}

		boolean shouldListen(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity);

		default boolean shouldApplyWoolOcclusion(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity) {
			if (gameEvent == GameEvent.BLOCK_PLACE) {
				BlockState blockState = level.getBlockState(blockPos);
				if (blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
					BlockPos blockPos2 = blockPos.below();
					BlockState blockState2 = level.getBlockState(blockPos2);
					return !blockState2.is(BlockTags.SKIP_OCCLUDE_VIBRATION_WHEN_ABOVE);
				}
			}

			return true;
		}

		void onSignalReceive(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, int i);
	}
}
