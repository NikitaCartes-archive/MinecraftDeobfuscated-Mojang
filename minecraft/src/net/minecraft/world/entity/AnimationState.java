package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.util.Mth;

public class AnimationState {
	private static final long STARTED = Long.MIN_VALUE;
	private static final long STOPPED = Long.MAX_VALUE;
	private long lastTime = Long.MAX_VALUE;
	private long accumulatedTime;

	public void start() {
		this.lastTime = Long.MIN_VALUE;
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

	public void updateTime(float f, float g) {
		if (this.isStarted()) {
			long l = Mth.lfloor((double)(f * 1000.0F / 20.0F));
			if (this.lastTime == Long.MIN_VALUE) {
				this.lastTime = l;
			}

			this.accumulatedTime = this.accumulatedTime + (long)((float)(l - this.lastTime) * g);
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
