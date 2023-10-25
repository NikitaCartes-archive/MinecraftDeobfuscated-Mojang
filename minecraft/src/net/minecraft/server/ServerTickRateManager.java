package net.minecraft.server;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.TickRateManager;

public class ServerTickRateManager extends TickRateManager {
	private long remainingSprintTicks = 0L;
	private long sprintTickStartTime = 0L;
	private long sprintTimeSpend = 0L;
	private long scheduledCurrentSprintTicks = 0L;
	private boolean previousIsFrozen = false;
	private final MinecraftServer server;

	public ServerTickRateManager(MinecraftServer minecraftServer) {
		this.server = minecraftServer;
	}

	public boolean isSprinting() {
		return this.scheduledCurrentSprintTicks > 0L;
	}

	@Override
	public void setFrozen(boolean bl) {
		super.setFrozen(bl);
		this.updateStateToClients();
	}

	private void updateStateToClients() {
		this.server.getPlayerList().broadcastAll(ClientboundTickingStatePacket.from(this));
	}

	private void updateStepTicks() {
		this.server.getPlayerList().broadcastAll(ClientboundTickingStepPacket.from(this));
	}

	public boolean stepGameIfPaused(int i) {
		if (!this.isFrozen()) {
			return false;
		} else {
			this.frozenTicksToRun = i;
			this.updateStepTicks();
			return true;
		}
	}

	public boolean stopStepping() {
		if (this.frozenTicksToRun > 0) {
			this.frozenTicksToRun = 0;
			this.updateStepTicks();
			return true;
		} else {
			return false;
		}
	}

	public boolean stopSprinting() {
		if (this.remainingSprintTicks > 0L) {
			this.finishTickSprint();
			return true;
		} else {
			return false;
		}
	}

	public boolean requestGameToSprint(int i) {
		boolean bl = this.remainingSprintTicks > 0L;
		this.sprintTimeSpend = 0L;
		this.scheduledCurrentSprintTicks = (long)i;
		this.remainingSprintTicks = (long)i;
		this.previousIsFrozen = this.isFrozen();
		this.setFrozen(false);
		return bl;
	}

	private void finishTickSprint() {
		long l = this.scheduledCurrentSprintTicks - this.remainingSprintTicks;
		double d = Math.max(1.0, (double)this.sprintTimeSpend) / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND;
		int i = (int)((double)(TimeUtil.MILLISECONDS_PER_SECOND * l) / d);
		String string = String.format("%.2f", l == 0L ? (double)this.millisecondsPerTick() : d / (double)l);
		this.scheduledCurrentSprintTicks = 0L;
		this.sprintTimeSpend = 0L;
		this.server.createCommandSourceStack().sendSuccess(() -> Component.translatable("commands.tick.sprint.report", i, string), true);
		this.remainingSprintTicks = 0L;
		this.setFrozen(this.previousIsFrozen);
		this.server.onTickRateChanged();
	}

	public boolean checkShouldSprintThisTick() {
		if (!this.runGameElements) {
			return false;
		} else if (this.remainingSprintTicks > 0L) {
			this.sprintTickStartTime = System.nanoTime();
			this.remainingSprintTicks--;
			return true;
		} else {
			this.finishTickSprint();
			return false;
		}
	}

	public void endTickWork() {
		this.sprintTimeSpend = this.sprintTimeSpend + (System.nanoTime() - this.sprintTickStartTime);
	}

	@Override
	public void setTickRate(float f) {
		super.setTickRate(f);
		this.server.onTickRateChanged();
		this.updateStateToClients();
	}

	public void updateJoiningPlayer(ServerPlayer serverPlayer) {
		serverPlayer.connection.send(ClientboundTickingStatePacket.from(this));
		serverPlayer.connection.send(ClientboundTickingStepPacket.from(this));
	}
}
