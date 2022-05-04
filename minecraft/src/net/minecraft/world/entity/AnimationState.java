package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.Util;

public class AnimationState {
	private static final long STOPPED = Long.MAX_VALUE;
	private long lastTime = Long.MAX_VALUE;
	private long accumulatedTime;

	public void start() {
		this.lastTime = Util.getMillis();
		this.accumulatedTime = 0L;
	}

	public void startIfStopped() {
		if (!this.isStarted()) {
			this.start();
		}
	}

	public void stop() {
		this.lastTime = Long.MAX_VALUE;
	}

	public void ifStarted(Consumer<AnimationState> consumer) {
		if (this.isStarted()) {
			consumer.accept(this);
		}
	}

	public void updateTime(boolean bl, float f) {
		if (this.isStarted()) {
			long l = Util.getMillis();
			if (!bl) {
				this.accumulatedTime = this.accumulatedTime + (long)((float)(l - this.lastTime) * f);
			}

			this.lastTime = l;
		}
	}

	public long getAccumulatedTime() {
		return this.accumulatedTime;
	}

	public boolean isStarted() {
		return this.lastTime != Long.MAX_VALUE;
	}
}
