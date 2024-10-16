package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
	private static final Logger LOGGER = LogUtils.getLogger();
	private VibrationSystem.Data vibrationData;
	private final VibrationSystem.Listener vibrationListener;
	private final VibrationSystem.User vibrationUser = this.createVibrationUser();
	private int lastVibrationFrequency;

	protected SculkSensorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
		this.vibrationData = new VibrationSystem.Data();
		this.vibrationListener = new VibrationSystem.Listener(this);
	}

	public SculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
		this(BlockEntityType.SCULK_SENSOR, blockPos, blockState);
	}

	public VibrationSystem.User createVibrationUser() {
		return new SculkSensorBlockEntity.VibrationUser(this.getBlockPos());
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.lastVibrationFrequency = compoundTag.getInt("last_vibration_frequency");
		RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
		if (compoundTag.contains("listener", 10)) {
			VibrationSystem.Data.CODEC
				.parse(registryOps, compoundTag.getCompound("listener"))
				.resultOrPartial(string -> LOGGER.error("Failed to parse vibration listener for Sculk Sensor: '{}'", string))
				.ifPresent(data -> this.vibrationData = data);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putInt("last_vibration_frequency", this.lastVibrationFrequency);
		RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
		VibrationSystem.Data.CODEC
			.encodeStart(registryOps, this.vibrationData)
			.resultOrPartial(string -> LOGGER.error("Failed to encode vibration listener for Sculk Sensor: '{}'", string))
			.ifPresent(tag -> compoundTag.put("listener", tag));
	}

	@Override
	public VibrationSystem.Data getVibrationData() {
		return this.vibrationData;
	}

	@Override
	public VibrationSystem.User getVibrationUser() {
		return this.vibrationUser;
	}

	public int getLastVibrationFrequency() {
		return this.lastVibrationFrequency;
	}

	public void setLastVibrationFrequency(int i) {
		this.lastVibrationFrequency = i;
	}

	public VibrationSystem.Listener getListener() {
		return this.vibrationListener;
	}

	protected class VibrationUser implements VibrationSystem.User {
		public static final int LISTENER_RANGE = 8;
		protected final BlockPos blockPos;
		private final PositionSource positionSource;

		public VibrationUser(final BlockPos blockPos) {
			this.blockPos = blockPos;
			this.positionSource = new BlockPositionSource(blockPos);
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
		public boolean canTriggerAvoidVibration() {
			return true;
		}

		@Override
		public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable GameEvent.Context context) {
			return !blockPos.equals(this.blockPos) || !holder.is(GameEvent.BLOCK_DESTROY) && !holder.is(GameEvent.BLOCK_PLACE)
				? SculkSensorBlock.canActivate(SculkSensorBlockEntity.this.getBlockState())
				: false;
		}

		@Override
		public void onReceiveVibration(
			ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity2, float f
		) {
			BlockState blockState = SculkSensorBlockEntity.this.getBlockState();
			if (SculkSensorBlock.canActivate(blockState)) {
				SculkSensorBlockEntity.this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(holder));
				int i = VibrationSystem.getRedstoneStrengthForDistance(f, this.getListenerRadius());
				if (blockState.getBlock() instanceof SculkSensorBlock sculkSensorBlock) {
					sculkSensorBlock.activate(entity, serverLevel, this.blockPos, blockState, i, SculkSensorBlockEntity.this.getLastVibrationFrequency());
				}
			}
		}

		@Override
		public void onDataChanged() {
			SculkSensorBlockEntity.this.setChanged();
		}

		@Override
		public boolean requiresAdjacentChunksToBeTicking() {
			return true;
		}
	}
}
