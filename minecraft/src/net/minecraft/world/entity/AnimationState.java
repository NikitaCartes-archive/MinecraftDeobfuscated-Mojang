package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.util.Mth;

public class AnimationState {
	private static final long STOPPED = Long.MAX_VALUE;
	private long lastTime = Long.MAX_VALUE;
	private long accumulatedTime;

	public void start(int i) {
		this.lastTime = (long)i * 1000L / 20L;
		this.accumulatedTime = 0L;
	}

	public void startIfStopped(int i) {
		if (!this.isStarted()) {
			this.start(i);
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
