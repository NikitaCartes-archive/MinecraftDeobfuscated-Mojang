package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private VibrationListener listener;
	private final VibrationListener.Config vibrationConfig = this.createVibrationConfig();
	private int lastVibrationFrequency;

	protected SculkSensorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
		this.listener = new VibrationListener(new BlockPositionSource(this.worldPosition), this.vibrationConfig);
	}

	public SculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
		this(BlockEntityType.SCULK_SENSOR, blockPos, blockState);
	}

	public VibrationListener.Config createVibrationConfig() {
		return new SculkSensorBlockEntity.VibrationConfig(this);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.lastVibrationFrequency = compoundTag.getInt("last_vibration_frequency");
		if (compoundTag.contains("listener", 10)) {
			VibrationListener.codec(this.vibrationConfig)
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getCompound("listener")))
				.resultOrPartial(LOGGER::error)
				.ifPresent(vibrationListener -> this.listener = vibrationListener);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		compoundTag.putInt("last_vibration_frequency", this.lastVibrationFrequency);
		VibrationListener.codec(this.vibrationConfig)
			.encodeStart(NbtOps.INSTANCE, this.listener)
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("listener", tag));
	}

	public VibrationListener getListener() {
		return this.listener;
	}

	public int getLastVibrationFrequency() {
		return this.lastVibrationFrequency;
	}

	public void setLastVibrationFrequency(int i) {
		this.lastVibrationFrequency = i;
	}

	public static class VibrationConfig implements VibrationListener.Config {
		public static final int LISTENER_RANGE = 8;
		protected final SculkSensorBlockEntity sculkSensor;

		public VibrationConfig(SculkSensorBlockEntity sculkSensorBlockEntity) {
			this.sculkSensor = sculkSensorBlockEntity;
		}

		@Override
		public boolean canTriggerAvoidVibration() {
			return true;
		}

		@Override
		public boolean shouldListen(
			ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable GameEvent.Context context
		) {
			return !blockPos.equals(this.sculkSensor.getBlockPos()) || gameEvent != GameEvent.BLOCK_DESTROY && gameEvent != GameEvent.BLOCK_PLACE
				? SculkSensorBlock.canActivate(this.sculkSensor.getBlockState())
				: false;
		}

		@Override
		public void onSignalReceive(
			ServerLevel serverLevel,
			GameEventListener gameEventListener,
			BlockPos blockPos,
			GameEvent gameEvent,
			@Nullable Entity entity,
			@Nullable Entity entity2,
			float f
		) {
			BlockState blockState = this.sculkSensor.getBlockState();
			BlockPos blockPos2 = this.sculkSensor.getBlockPos();
			if (SculkSensorBlock.canActivate(blockState)) {
				this.sculkSensor.setLastVibrationFrequency(VibrationListener.getGameEventFrequency(gameEvent));
				int i = getRedstoneStrengthForDistance(f, gameEventListener.getListenerRadius());
				SculkSensorBlock.activate(entity, serverLevel, blockPos2, blockState, i, this.sculkSensor.getLastVibrationFrequency());
			}
		}

		@Override
		public void onSignalSchedule() {
			this.sculkSensor.setChanged();
		}

		@Override
		public int getListenerRadius() {
			return 8;
		}

		public static int getRedstoneStrengthForDistance(float f, int i) {
			double d = (double)f / (double)i;
			return Math.max(1, 15 - Mth.floor(d * 15.0));
		}
	}
}
