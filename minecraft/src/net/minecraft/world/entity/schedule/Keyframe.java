package net.minecraft.world.entity.schedule;

public class Keyframe {
	private final int timeStamp;
	private final float value;

	public Keyframe(int i, float f) {
		this.timeStamp = i;
		this.value = f;
	}

	public int getTimeStamp() {
		return this.timeStamp;
	}

	public float getValue() {
		return this.value;
	}
}
