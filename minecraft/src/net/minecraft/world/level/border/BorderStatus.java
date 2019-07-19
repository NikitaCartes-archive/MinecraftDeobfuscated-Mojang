package net.minecraft.world.level.border;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum BorderStatus {
	GROWING(4259712),
	SHRINKING(16724016),
	STATIONARY(2138367);

	private final int color;

	private BorderStatus(int j) {
		this.color = j;
	}

	public int getColor() {
		return this.color;
	}
}
