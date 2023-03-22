package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
	public CalibratedSculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CALIBRATED_SCULK_SENSOR, blockPos, blockState);
	}

	@Override
	public VibrationListener.Config createVibrationConfig() {
		return new CalibratedSculkSensorBlockEntity.VibrationConfig(this);
	}

	public static class VibrationConfig extends SculkSensorBlockEntity.VibrationConfig {
		public VibrationConfig(SculkSensorBlockEntity sculkSensorBlockEntity) {
			super(sculkSensorBlockEntity);
		}

		@Override
		public boolean shouldListen(
			ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable GameEvent.Context context
		) {
			BlockPos blockPos2 = this.sculkSensor.getBlockPos();
			int i = this.getBackSignal(serverLevel, blockPos2, this.sculkSensor.getBlockState());
			return i != 0 && VibrationListener.getGameEventFrequency(gameEvent) != i
				? false
				: super.shouldListen(serverLevel, gameEventListener, blockPos, gameEvent, context);
		}

		private int getBackSignal(Level level, BlockPos blockPos, BlockState blockState) {
			Direction direction = ((Direction)blockState.getValue(CalibratedSculkSensorBlock.FACING)).getOpposite();
			return level.getControlInputSignal(blockPos.relative(direction), direction, false);
		}
	}
}
