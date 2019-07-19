package com.mojang.math;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Vector4f {
	private final float[] values;

	public Vector4f() {
		this.values = new float[4];
	}

	public Vector4f(float f, float g, float h, float i) {
		this.values = new float[]{f, g, h, i};
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Vector4f vector4f = (Vector4f)object;
			return Arrays.equals(this.values, vector4f.values);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Arrays.hashCode(this.values);
	}

	public float x() {
		return this.values[0];
	}

	public float y() {
		return this.values[1];
	}

	public float z() {
		return this.values[2];
	}

	public float w() {
		return this.values[3];
	}

	public void mul(Vector3f vector3f) {
		this.values[0] = this.values[0] * vector3f.x();
		this.values[1] = this.values[1] * vector3f.y();
		this.values[2] = this.values[2] * vector3f.z();
	}

	public void set(float f, float g, float h, float i) {
		this.values[0] = f;
		this.values[1] = g;
		this.values[2] = h;
		this.values[3] = i;
	}

	public void transform(Quaternion quaternion) {
		Quaternion quaternion2 = new Quaternion(quaternion);
		quaternion2.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
		Quaternion quaternion3 = new Quaternion(quaternion);
		quaternion3.conj();
		quaternion2.mul(quaternion3);
		this.set(quaternion2.i(), quaternion2.j(), quaternion2.k(), this.w());
	}
}
