package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
	private static final int CHECK_TICK_DELAY = 5;
	private int openCount;

	protected abstract void onOpen(Level level, BlockPos blockPos, BlockState blockState);

	protected abstract void onClose(Level level, BlockPos blockPos, BlockState blockState);

	protected abstract void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int i, int j);

	protected abstract boolean isOwnContainer(Player player);

	public void incrementOpeners(Player player, Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.openCount++;
		if (i == 0) {
			this.onOpen(level, blockPos, blockState);
			level.gameEvent(player, GameEvent.CONTAINER_OPEN, blockPos);
			scheduleRecheck(level, blockPos, blockState);
		}

		this.openerCountChanged(level, blockPos, blockState, i, this.openCount);
	}

	public void decrementOpeners(Player player, Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.openCount--;
		if (this.openCount == 0) {
			this.onClose(level, blockPos, blockState);
			level.gameEvent(player, GameEvent.CONTAINER_CLOSE, blockPos);
		}

		this.openerCountChanged(level, blockPos, blockState, i, this.openCount);
	}

	private int getOpenCount(Level level, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		float f = 5.0F;
		AABB aABB = new AABB(
			(double)((float)i - 5.0F),
			(double)((float)j - 5.0F),
			(double)((float)k - 5.0F),
			(double)((float)(i + 1) + 5.0F),
			(double)((float)(j + 1) + 5.0F),
			(double)((float)(k + 1) + 5.0F)
		);
		return level.getEntities(EntityTypeTest.forClass(Player.class), aABB, this::isOwnContainer).size();
	}

	public void recheckOpeners(Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.getOpenCount(level, blockPos);
		int j = this.openCount;
		if (j != i) {
			boolean bl = i != 0;
			boolean bl2 = j != 0;
			if (bl && !bl2) {
				this.onOpen(level, blockPos, blockState);
				level.gameEvent(null, GameEvent.CONTAINER_OPEN, blockPos);
			} else if (!bl) {
				this.onClose(level, blockPos, blockState);
				level.gameEvent(null, GameEvent.CONTAINER_CLOSE, blockPos);
			}

			this.openCount = i;
		}

		this.openerCountChanged(level, blockPos, blockState, j, i);
		if (i > 0) {
			scheduleRecheck(level, blockPos, blockState);
		}
	}

	public int getOpenerCount() {
		return this.openCount;
	}

	private static void scheduleRecheck(Level level, BlockPos blockPos, BlockState blockState) {
		level.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 5);
	}
}
