package com.mojang.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Vector3d {
	public double x;
	public double y;
	public double z;

	public Vector3d(double d, double e, double f) {
		this.x = d;
		this.y = e;
		this.z = f;
	}
}
