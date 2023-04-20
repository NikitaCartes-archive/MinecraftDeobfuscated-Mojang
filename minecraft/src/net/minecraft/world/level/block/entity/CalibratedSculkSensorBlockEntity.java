package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
	public CalibratedSculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CALIBRATED_SCULK_SENSOR, blockPos, blockState);
	}

	@Override
	public VibrationSystem.User createVibrationUser() {
		return new CalibratedSculkSensorBlockEntity.VibrationUser(this.getBlockPos());
	}

	protected class VibrationUser extends SculkSensorBlockEntity.VibrationUser {
		public VibrationUser(BlockPos blockPos) {
			super(blockPos);
		}

		@Override
		public int getListenerRadius() {
			return 16;
		}

		@Override
		public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, GameEvent gameEvent, @Nullable GameEvent.Context context) {
			int i = this.getBackSignal(serverLevel, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());
			return i != 0 && VibrationSystem.getGameEventFrequency(gameEvent) != i ? false : super.canReceiveVibration(serverLevel, blockPos, gameEvent, context);
		}

		private int getBackSignal(Level level, BlockPos blockPos, BlockState blockState) {
			Direction direction = ((Direction)blockState.getValue(CalibratedSculkSensorBlock.FACING)).getOpposite();
			return level.getSignal(blockPos.relative(direction), direction);
		}
	}
}
