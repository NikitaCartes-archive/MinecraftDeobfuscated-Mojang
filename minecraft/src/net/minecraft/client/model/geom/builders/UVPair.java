package net.minecraft.client.model.geom.builders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UVPair {
	private final float u;
	private final float v;

	public UVPair(float f, float g) {
		this.u = f;
		this.v = g;
	}

	public float u() {
		return this.u;
	}

	public float v() {
		return this.v;
	}

	public String toString() {
		return "(" + this.u + "," + this.v + ")";
	}
}
