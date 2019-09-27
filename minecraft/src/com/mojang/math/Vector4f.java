package com.mojang.math;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

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

	public void mul(Vector3f vector3f) {
		this.values[0] = this.values[0] * vector3f.x();
		this.values[1] = this.values[1] * vector3f.y();
		this.values[2] = this.values[2] * vector3f.z();
	}

	public float dot(Vector4f vector4f) {
		float f = 0.0F;

		for (int i = 0; i < 4; i++) {
			f += this.values[i] * vector4f.values[i];
		}

		return f;
	}

	public boolean normalize() {
		float f = 0.0F;

		for (int i = 0; i < 4; i++) {
			f += this.values[i] * this.values[i];
		}

		if ((double)f < 1.0E-5) {
			return false;
		} else {
			float g = Mth.fastInvSqrt(f);

			for (int j = 0; j < 4; j++) {
				this.values[j] = this.values[j] * g;
			}

			return true;
		}
	}

	public void transform(Matrix4f matrix4f) {
		float[] fs = Arrays.copyOf(this.values, 4);

		for (int i = 0; i < 4; i++) {
			this.values[i] = 0.0F;

			for (int j = 0; j < 4; j++) {
				this.values[i] = this.values[i] + matrix4f.get(i, j) * fs[j];
			}
		}
	}

	public void perspectiveDivide() {
		this.values[0] = this.values[0] / this.values[3];
		this.values[1] = this.values[1] / this.values[3];
		this.values[2] = this.values[2] / this.values[3];
		this.values[3] = 1.0F;
	}
}
