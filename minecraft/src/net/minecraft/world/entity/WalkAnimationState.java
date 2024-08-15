package net.minecraft.world.entity;

import net.minecraft.util.Mth;

public class WalkAnimationState {
	private float speedOld;
	private float speed;
	private float position;
	private float positionScale = 1.0F;

	public void setSpeed(float f) {
		this.speed = f;
	}

	public void update(float f, float g, float h) {
		this.speedOld = this.speed;
		this.speed = this.speed + (f - this.speed) * g;
		this.position = this.position + this.speed;
		this.positionScale = h;
	}

	public void stop() {
		this.speedOld = 0.0F;
		this.speed = 0.0F;
		this.position = 0.0F;
	}

	public float speed() {
		return this.speed;
	}

	public float speed(float f) {
		return Math.min(Mth.lerp(f, this.speedOld, this.speed), 1.0F);
	}

	public float position() {
		return this.position * this.positionScale;
	}

	public float position(float f) {
		return (this.position - this.speed * (1.0F - f)) * this.positionScale;
	}

	public boolean isMoving() {
		return this.speed > 1.0E-5F;
	}
}
