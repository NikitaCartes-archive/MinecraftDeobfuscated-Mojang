package net.minecraft.network.chat;

import javax.annotation.Nullable;

public class ChatPreviewThrottler {
	private boolean sentRequestThisTick;
	@Nullable
	private Runnable pendingRequest;

	public void tick() {
		Runnable runnable = this.pendingRequest;
		if (runnable != null) {
			runnable.run();
			this.pendingRequest = null;
		}

		this.sentRequestThisTick = false;
	}

	public void execute(Runnable runnable) {
		if (this.sentRequestThisTick) {
			this.pendingRequest = runnable;
		} else {
			runnable.run();
			this.sentRequestThisTick = true;
			this.pendingRequest = null;
		}
	}
}
