package com.mojang.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@FunctionalInterface
public interface Axis {
	Axis XN = f -> new Quaternionf().rotationX(-f);
	Axis XP = f -> new Quaternionf().rotationX(f);
	Axis YN = f -> new Quaternionf().rotationY(-f);
	Axis YP = f -> new Quaternionf().rotationY(f);
	Axis ZN = f -> new Quaternionf().rotationZ(-f);
	Axis ZP = f -> new Quaternionf().rotationZ(f);

	static Axis of(Vector3f vector3f) {
		return f -> new Quaternionf().rotationAxis(f, vector3f);
	}

	Quaternionf rotation(float f);

	default Quaternionf rotationDegrees(float f) {
		return this.rotation(f * (float) (Math.PI / 180.0));
	}
}
