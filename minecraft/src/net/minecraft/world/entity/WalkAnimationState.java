package net.minecraft.world.entity;

import net.minecraft.util.Mth;

public class WalkAnimationState {
	private float speedOld;
	private float speed;
	private float position;

	public void setSpeed(float f) {
		this.speed = f;
	}

	public void update(float f, float g) {
		this.speedOld = this.speed;
		this.speed = this.speed + (f - this.speed) * g;
		if (this.isMoving()) {
			this.position = this.position + this.speed;
		} else {
			this.position = 0.0F;
		}
	}

	public float speed() {
		return this.speed;
	}

	public float speed(float f) {
		return Mth.lerp(f, this.speedOld, this.speed);
	}

	public float position() {
		return this.position;
	}

	public float position(float f) {
		return this.position - this.speed * (1.0F - f);
	}

	public boolean isMoving() {
		return this.speed > 1.0E-5F;
	}
}
