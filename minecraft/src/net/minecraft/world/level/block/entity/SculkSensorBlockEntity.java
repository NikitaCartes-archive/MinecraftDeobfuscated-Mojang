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

public class SculkSensorBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private VibrationListener listener;
	private int lastVibrationFrequency;

	public SculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_SENSOR, blockPos, blockState);
		this.listener = new VibrationListener(
			new BlockPositionSource(this.worldPosition), ((SculkSensorBlock)blockState.getBlock()).getListenerRange(), this, null, 0.0F, 0
		);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.lastVibrationFrequency = compoundTag.getInt("last_vibration_frequency");
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
		compoundTag.putInt("last_vibration_frequency", this.lastVibrationFrequency);
		VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.listener).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("listener", tag));
	}

	public VibrationListener getListener() {
		return this.listener;
	}

	public int getLastVibrationFrequency() {
		return this.lastVibrationFrequency;
	}

	@Override
	public boolean canTriggerAvoidVibration() {
		return true;
	}

	@Override
	public boolean shouldListen(
		ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable GameEvent.Context context
	) {
		return !this.isRemoved() && (!blockPos.equals(this.getBlockPos()) || gameEvent != GameEvent.BLOCK_DESTROY && gameEvent != GameEvent.BLOCK_PLACE)
			? SculkSensorBlock.canActivate(this.getBlockState())
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
		BlockState blockState = this.getBlockState();
		if (SculkSensorBlock.canActivate(blockState)) {
			this.lastVibrationFrequency = SculkSensorBlock.VIBRATION_FREQUENCY_FOR_EVENT.getInt(gameEvent);
			SculkSensorBlock.activate(entity, serverLevel, this.worldPosition, blockState, getRedstoneStrengthForDistance(f, gameEventListener.getListenerRadius()));
		}
	}

	@Override
	public void onSignalSchedule() {
		this.setChanged();
	}

	public static int getRedstoneStrengthForDistance(float f, int i) {
		double d = (double)f / (double)i;
		return Math.max(1, 15 - Mth.floor(d * 15.0));
	}

	public void setLastVibrationFrequency(int i) {
		this.lastVibrationFrequency = i;
	}
}
