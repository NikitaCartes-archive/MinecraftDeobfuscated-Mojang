package com.mojang.blaze3d.shaders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum FogShape {
	SPHERE(0),
	CYLINDER(1);

	private final int index;

	private FogShape(int j) {
		this.index = j;
	}

	public int getIndex() {
		return this.index;
	}
}
