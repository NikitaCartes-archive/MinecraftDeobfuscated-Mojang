package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;

public class SculkShriekerBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
	public static final int LISTENER_RADIUS = 8;
	private final VibrationListener listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this);

	public SculkShriekerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_SHRIEKER, blockPos, blockState);
	}

	public VibrationListener getListener() {
		return this.listener;
	}

	@Override
	public boolean isValidVibration(GameEvent gameEvent, @Nullable Entity entity) {
		return true;
	}

	@Override
	public boolean shouldListen(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity) {
		if (entity instanceof Projectile projectile) {
			entity = projectile.getOwner();
		}

		return entity instanceof Player
			&& gameEvent == GameEvent.SCULK_SENSOR_TENDRILS_CLICKING
			&& SculkShriekerBlock.canShriek((ServerLevel)level, this.getBlockPos(), this.getBlockState());
	}

	@Override
	public void onSignalReceive(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, int i) {
		BlockState blockState = this.getBlockState();
		if (!level.isClientSide() && SculkShriekerBlock.canShriek((ServerLevel)level, this.getBlockPos(), blockState)) {
			SculkShriekerBlock.shriek((ServerLevel)level, blockState, this.getBlockPos());
		}
	}
}
