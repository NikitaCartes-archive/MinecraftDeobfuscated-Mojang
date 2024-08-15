package net.minecraft.util;

public class BinaryAnimator {
	private final int animationLength;
	private final BinaryAnimator.EasingFunction easingFunction;
	private int ticks;
	private int ticksOld;

	public BinaryAnimator(int i, BinaryAnimator.EasingFunction easingFunction) {
		this.animationLength = i;
		this.easingFunction = easingFunction;
	}

	public BinaryAnimator(int i) {
		this(i, f -> f);
	}

	public void tick(boolean bl) {
		this.ticksOld = this.ticks;
		if (bl) {
			if (this.ticks < this.animationLength) {
				this.ticks++;
			}
		} else if (this.ticks > 0) {
			this.ticks--;
		}
	}

	public float getFactor(float f) {
		float g = Mth.lerp(f, (float)this.ticksOld, (float)this.ticks) / (float)this.animationLength;
		return this.easingFunction.apply(g);
	}

	public interface EasingFunction {
		float apply(float f);
	}
}
