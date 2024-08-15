package net.minecraft.world.entity;

import java.util.function.Consumer;

public class AnimationState {
	private static final int STOPPED = Integer.MIN_VALUE;
	private int startTick = Integer.MIN_VALUE;

	public void start(int i) {
		this.startTick = i;
	}

	public void startIfStopped(int i) {
		if (!this.isStarted()) {
			this.start(i);
		}
	}

	public void animateWhen(boolean bl, int i) {
		if (bl) {
			this.startIfStopped(i);
		} else {
			this.stop();
		}
	}

	public void stop() {
		this.startTick = Integer.MIN_VALUE;
	}

	public void ifStarted(Consumer<AnimationState> consumer) {
		if (this.isStarted()) {
			consumer.accept(this);
		}
	}

	public void fastForward(int i, float f) {
		if (this.isStarted()) {
			this.startTick -= (int)((float)i * f);
		}
	}

	public long getTimeInMillis(float f) {
		float g = f - (float)this.startTick;
		return (long)(g * 50.0F);
	}

	public boolean isStarted() {
		return this.startTick != Integer.MIN_VALUE;
	}

	public void copyFrom(AnimationState animationState) {
		this.startTick = animationState.startTick;
	}
}
