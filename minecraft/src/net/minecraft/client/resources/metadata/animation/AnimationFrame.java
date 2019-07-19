package net.minecraft.client.resources.metadata.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AnimationFrame {
	private final int index;
	private final int time;

	public AnimationFrame(int i) {
		this(i, -1);
	}

	public AnimationFrame(int i, int j) {
		this.index = i;
		this.time = j;
	}

	public boolean isTimeUnknown() {
		return this.time == -1;
	}

	public int getTime() {
		return this.time;
	}

	public int getIndex() {
		return this.index;
	}
}
