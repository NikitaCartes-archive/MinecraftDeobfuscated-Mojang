package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.Util;

public class AnimationState {
	private static final long STOPPED = Long.MAX_VALUE;
	private long startTime = Long.MAX_VALUE;

	public void start() {
		this.startTime = Util.getMillis();
	}

	public void startIfStopped() {
		if (!this.isStarted()) {
			this.start();
		}
	}

	public void stop() {
		this.startTime = Long.MAX_VALUE;
	}

	public long startTime() {
		return this.startTime;
	}

	public void ifStarted(Consumer<AnimationState> consumer) {
		if (this.isStarted()) {
			consumer.accept(this);
		}
	}

	private boolean isStarted() {
		return this.startTime != Long.MAX_VALUE;
	}
}
