package net.minecraft.client.resources.metadata.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AnimationFrame {
	public static final int UNKNOWN_FRAME_TIME = -1;
	private final int index;
	private final int time;

	public AnimationFrame(int i) {
		this(i, -1);
	}

	public AnimationFrame(int i, int j) {
		this.index = i;
		this.time = j;
	}

	public int getTime(int i) {
		return this.time == -1 ? i : this.time;
	}

	public int getIndex() {
		return this.index;
	}
}
