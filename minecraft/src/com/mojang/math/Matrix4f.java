package com.mojang.math;

import java.nio.FloatBuffer;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Matrix4f {
	private final float[] values;

	public Matrix4f() {
		this(new float[16]);
	}

	public Matrix4f(float[] fs) {
		this.values = fs;
	}

	public Matrix4f(Quaternion quaternion) {
		this();
		float f = quaternion.i();
		float g = quaternion.j();
		float h = quaternion.k();
		float i = quaternion.r();
		float j = 2.0F * f * f;
		float k = 2.0F * g * g;
		float l = 2.0F * h * h;
		this.set(0, 0, 1.0F - k - l);
		this.set(1, 1, 1.0F - l - j);
		this.set(2, 2, 1.0F - j - k);
		this.set(3, 3, 1.0F);
		float m = f * g;
		float n = g * h;
		float o = h * f;
		float p = f * i;
		float q = g * i;
		float r = h * i;
		this.set(1, 0, 2.0F * (m + r));
		this.set(0, 1, 2.0F * (m - r));
		this.set(2, 0, 2.0F * (o - q));
		this.set(0, 2, 2.0F * (o + q));
		this.set(2, 1, 2.0F * (n + p));
		this.set(1, 2, 2.0F * (n - p));
	}

	public Matrix4f(Matrix4f matrix4f) {
		this(Arrays.copyOf(matrix4f.values, 16));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Matrix4f matrix4f = (Matrix4f)object;
			return Arrays.equals(this.values, matrix4f.values);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Arrays.hashCode(this.values);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Matrix4f:\n");

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				stringBuilder.append(this.values[i + j * 4]);
				if (j != 3) {
					stringBuilder.append(" ");
				}
			}

			stringBuilder.append("\n");
		}

		return stringBuilder.toString();
	}

	public void store(FloatBuffer floatBuffer) {
		this.store(floatBuffer, false);
	}

	public void store(FloatBuffer floatBuffer, boolean bl) {
		if (bl) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					floatBuffer.put(j * 4 + i, this.values[i * 4 + j]);
				}
			}
		} else {
			floatBuffer.put(this.values);
		}
	}

	public void setIdentity() {
		this.values[0] = 1.0F;
		this.values[1] = 0.0F;
		this.values[2] = 0.0F;
		this.values[3] = 0.0F;
		this.values[4] = 0.0F;
		this.values[5] = 1.0F;
		this.values[6] = 0.0F;
		this.values[7] = 0.0F;
		this.values[8] = 0.0F;
		this.values[9] = 0.0F;
		this.values[10] = 1.0F;
		this.values[11] = 0.0F;
		this.values[12] = 0.0F;
		this.values[13] = 0.0F;
		this.values[14] = 0.0F;
		this.values[15] = 1.0F;
	}

	public float get(int i, int j) {
		return this.values[4 * j + i];
	}

	public void set(int i, int j, float f) {
		this.values[4 * j + i] = f;
	}

	public float adjugateAndDet() {
		float f = this.det2(0, 1, 0, 1);
		float g = this.det2(0, 1, 0, 2);
		float h = this.det2(0, 1, 0, 3);
		float i = this.det2(0, 1, 1, 2);
		float j = this.det2(0, 1, 1, 3);
		float k = this.det2(0, 1, 2, 3);
		float l = this.det2(2, 3, 0, 1);
		float m = this.det2(2, 3, 0, 2);
		float n = this.det2(2, 3, 0, 3);
		float o = this.det2(2, 3, 1, 2);
		float p = this.det2(2, 3, 1, 3);
		float q = this.det2(2, 3, 2, 3);
		float r = this.get(1, 1) * q - this.get(1, 2) * p + this.get(1, 3) * o;
		float s = -this.get(1, 0) * q + this.get(1, 2) * n - this.get(1, 3) * m;
		float t = this.get(1, 0) * p - this.get(1, 1) * n + this.get(1, 3) * l;
		float u = -this.get(1, 0) * o + this.get(1, 1) * m - this.get(1, 2) * l;
		float v = -this.get(0, 1) * q + this.get(0, 2) * p - this.get(0, 3) * o;
		float w = this.get(0, 0) * q - this.get(0, 2) * n + this.get(0, 3) * m;
		float x = -this.get(0, 0) * p + this.get(0, 1) * n - this.get(0, 3) * l;
		float y = this.get(0, 0) * o - this.get(0, 1) * m + this.get(0, 2) * l;
		float z = this.get(3, 1) * k - this.get(3, 2) * j + this.get(3, 3) * i;
		float aa = -this.get(3, 0) * k + this.get(3, 2) * h - this.get(3, 3) * g;
		float ab = this.get(3, 0) * j - this.get(3, 1) * h + this.get(3, 3) * f;
		float ac = -this.get(3, 0) * i + this.get(3, 1) * g - this.get(3, 2) * f;
		float ad = -this.get(2, 1) * k + this.get(2, 2) * j - this.get(2, 3) * i;
		float ae = this.get(2, 0) * k - this.get(2, 2) * h + this.get(2, 3) * g;
		float af = -this.get(2, 0) * j + this.get(2, 1) * h - this.get(2, 3) * f;
		float ag = this.get(2, 0) * i - this.get(2, 1) * g + this.get(2, 2) * f;
		this.set(0, 0, r);
		this.set(1, 0, s);
		this.set(2, 0, t);
		this.set(3, 0, u);
		this.set(0, 1, v);
		this.set(1, 1, w);
		this.set(2, 1, x);
		this.set(3, 1, y);
		this.set(0, 2, z);
		this.set(1, 2, aa);
		this.set(2, 2, ab);
		this.set(3, 2, ac);
		this.set(0, 3, ad);
		this.set(1, 3, ae);
		this.set(2, 3, af);
		this.set(3, 3, ag);
		return f * q - g * p + h * o + i * n - j * m + k * l;
	}

	public void transpose() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < i; j++) {
				this.swap(i, j);
			}
		}
	}

	private void swap(int i, int j) {
		float f = this.values[i + j * 4];
		this.values[i + j * 4] = this.values[j + i * 4];
		this.values[j + i * 4] = f;
	}

	public boolean invert() {
		float f = this.adjugateAndDet();
		if (Math.abs(f) > 1.0E-6F) {
			this.multiply(f);
			return true;
		} else {
			return false;
		}
	}

	private float det2(int i, int j, int k, int l) {
		return this.get(i, k) * this.get(j, l) - this.get(i, l) * this.get(j, k);
	}

	public void multiply(Matrix4f matrix4f) {
		float[] fs = Arrays.copyOf(this.values, 16);

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				this.values[i + j * 4] = 0.0F;

				for (int k = 0; k < 4; k++) {
					this.values[i + j * 4] = this.values[i + j * 4] + fs[i + k * 4] * matrix4f.values[k + j * 4];
				}
			}
		}
	}

	public void multiply(Quaternion quaternion) {
		this.multiply(new Matrix4f(quaternion));
	}

	public void multiply(float f) {
		for (int i = 0; i < 16; i++) {
			this.values[i] = this.values[i] * f;
		}
	}

	public static Matrix4f perspective(double d, float f, float g, float h) {
		float i = (float)(1.0 / Math.tan(d * (float) (Math.PI / 180.0) / 2.0));
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.set(0, 0, i / f);
		matrix4f.set(1, 1, i);
		matrix4f.set(2, 2, (h + g) / (g - h));
		matrix4f.set(3, 2, -1.0F);
		matrix4f.set(2, 3, 2.0F * h * g / (g - h));
		return matrix4f;
	}

	public static Matrix4f orthographic(float f, float g, float h, float i) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.set(0, 0, 2.0F / f);
		matrix4f.set(1, 1, 2.0F / g);
		float j = i - h;
		matrix4f.set(2, 2, -2.0F / j);
		matrix4f.set(3, 3, 1.0F);
		matrix4f.set(0, 3, -1.0F);
		matrix4f.set(1, 3, -1.0F);
		matrix4f.set(2, 3, -(i + h) / j);
		return matrix4f;
	}

	public void translate(Vector3f vector3f) {
		this.set(0, 3, this.get(0, 3) + vector3f.x());
		this.set(1, 3, this.get(1, 3) + vector3f.y());
		this.set(2, 3, this.get(2, 3) + vector3f.z());
	}

	public Matrix4f copy() {
		return new Matrix4f((float[])this.values.clone());
	}
}
