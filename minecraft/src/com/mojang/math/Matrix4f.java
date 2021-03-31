package com.mojang.math;

import java.nio.FloatBuffer;

public final class Matrix4f {
	private static final int ORDER = 4;
	protected float m00;
	protected float m01;
	protected float m02;
	protected float m03;
	protected float m10;
	protected float m11;
	protected float m12;
	protected float m13;
	protected float m20;
	protected float m21;
	protected float m22;
	protected float m23;
	protected float m30;
	protected float m31;
	protected float m32;
	protected float m33;

	public Matrix4f() {
	}

	public Matrix4f(Matrix4f matrix4f) {
		this.m00 = matrix4f.m00;
		this.m01 = matrix4f.m01;
		this.m02 = matrix4f.m02;
		this.m03 = matrix4f.m03;
		this.m10 = matrix4f.m10;
		this.m11 = matrix4f.m11;
		this.m12 = matrix4f.m12;
		this.m13 = matrix4f.m13;
		this.m20 = matrix4f.m20;
		this.m21 = matrix4f.m21;
		this.m22 = matrix4f.m22;
		this.m23 = matrix4f.m23;
		this.m30 = matrix4f.m30;
		this.m31 = matrix4f.m31;
		this.m32 = matrix4f.m32;
		this.m33 = matrix4f.m33;
	}

	public Matrix4f(Quaternion quaternion) {
		float f = quaternion.i();
		float g = quaternion.j();
		float h = quaternion.k();
		float i = quaternion.r();
		float j = 2.0F * f * f;
		float k = 2.0F * g * g;
		float l = 2.0F * h * h;
		this.m00 = 1.0F - k - l;
		this.m11 = 1.0F - l - j;
		this.m22 = 1.0F - j - k;
		this.m33 = 1.0F;
		float m = f * g;
		float n = g * h;
		float o = h * f;
		float p = f * i;
		float q = g * i;
		float r = h * i;
		this.m10 = 2.0F * (m + r);
		this.m01 = 2.0F * (m - r);
		this.m20 = 2.0F * (o - q);
		this.m02 = 2.0F * (o + q);
		this.m21 = 2.0F * (n + p);
		this.m12 = 2.0F * (n - p);
	}

	public boolean isInteger() {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.m30 = 1.0F;
		matrix4f.m31 = 1.0F;
		matrix4f.m32 = 1.0F;
		matrix4f.m33 = 0.0F;
		Matrix4f matrix4f2 = this.copy();
		matrix4f2.multiply(matrix4f);
		return isInteger(matrix4f2.m00 / matrix4f2.m03)
			&& isInteger(matrix4f2.m10 / matrix4f2.m13)
			&& isInteger(matrix4f2.m20 / matrix4f2.m23)
			&& isInteger(matrix4f2.m01 / matrix4f2.m03)
			&& isInteger(matrix4f2.m11 / matrix4f2.m13)
			&& isInteger(matrix4f2.m21 / matrix4f2.m23)
			&& isInteger(matrix4f2.m02 / matrix4f2.m03)
			&& isInteger(matrix4f2.m12 / matrix4f2.m13)
			&& isInteger(matrix4f2.m22 / matrix4f2.m23);
	}

	private static boolean isInteger(float f) {
		return (double)Math.abs(f - (float)Math.round(f)) <= 1.0E-5;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Matrix4f matrix4f = (Matrix4f)object;
			return Float.compare(matrix4f.m00, this.m00) == 0
				&& Float.compare(matrix4f.m01, this.m01) == 0
				&& Float.compare(matrix4f.m02, this.m02) == 0
				&& Float.compare(matrix4f.m03, this.m03) == 0
				&& Float.compare(matrix4f.m10, this.m10) == 0
				&& Float.compare(matrix4f.m11, this.m11) == 0
				&& Float.compare(matrix4f.m12, this.m12) == 0
				&& Float.compare(matrix4f.m13, this.m13) == 0
				&& Float.compare(matrix4f.m20, this.m20) == 0
				&& Float.compare(matrix4f.m21, this.m21) == 0
				&& Float.compare(matrix4f.m22, this.m22) == 0
				&& Float.compare(matrix4f.m23, this.m23) == 0
				&& Float.compare(matrix4f.m30, this.m30) == 0
				&& Float.compare(matrix4f.m31, this.m31) == 0
				&& Float.compare(matrix4f.m32, this.m32) == 0
				&& Float.compare(matrix4f.m33, this.m33) == 0;
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
		i = 31 * i + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
		i = 31 * i + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
		i = 31 * i + (this.m03 != 0.0F ? Float.floatToIntBits(this.m03) : 0);
		i = 31 * i + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
		i = 31 * i + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
		i = 31 * i + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
		i = 31 * i + (this.m13 != 0.0F ? Float.floatToIntBits(this.m13) : 0);
		i = 31 * i + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
		i = 31 * i + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
		i = 31 * i + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
		i = 31 * i + (this.m23 != 0.0F ? Float.floatToIntBits(this.m23) : 0);
		i = 31 * i + (this.m30 != 0.0F ? Float.floatToIntBits(this.m30) : 0);
		i = 31 * i + (this.m31 != 0.0F ? Float.floatToIntBits(this.m31) : 0);
		i = 31 * i + (this.m32 != 0.0F ? Float.floatToIntBits(this.m32) : 0);
		return 31 * i + (this.m33 != 0.0F ? Float.floatToIntBits(this.m33) : 0);
	}

	private static int bufferIndex(int i, int j) {
		return j * 4 + i;
	}

	public void load(FloatBuffer floatBuffer) {
		this.m00 = floatBuffer.get(bufferIndex(0, 0));
		this.m01 = floatBuffer.get(bufferIndex(0, 1));
		this.m02 = floatBuffer.get(bufferIndex(0, 2));
		this.m03 = floatBuffer.get(bufferIndex(0, 3));
		this.m10 = floatBuffer.get(bufferIndex(1, 0));
		this.m11 = floatBuffer.get(bufferIndex(1, 1));
		this.m12 = floatBuffer.get(bufferIndex(1, 2));
		this.m13 = floatBuffer.get(bufferIndex(1, 3));
		this.m20 = floatBuffer.get(bufferIndex(2, 0));
		this.m21 = floatBuffer.get(bufferIndex(2, 1));
		this.m22 = floatBuffer.get(bufferIndex(2, 2));
		this.m23 = floatBuffer.get(bufferIndex(2, 3));
		this.m30 = floatBuffer.get(bufferIndex(3, 0));
		this.m31 = floatBuffer.get(bufferIndex(3, 1));
		this.m32 = floatBuffer.get(bufferIndex(3, 2));
		this.m33 = floatBuffer.get(bufferIndex(3, 3));
	}

	public void loadTransposed(FloatBuffer floatBuffer) {
		this.m00 = floatBuffer.get(bufferIndex(0, 0));
		this.m01 = floatBuffer.get(bufferIndex(1, 0));
		this.m02 = floatBuffer.get(bufferIndex(2, 0));
		this.m03 = floatBuffer.get(bufferIndex(3, 0));
		this.m10 = floatBuffer.get(bufferIndex(0, 1));
		this.m11 = floatBuffer.get(bufferIndex(1, 1));
		this.m12 = floatBuffer.get(bufferIndex(2, 1));
		this.m13 = floatBuffer.get(bufferIndex(3, 1));
		this.m20 = floatBuffer.get(bufferIndex(0, 2));
		this.m21 = floatBuffer.get(bufferIndex(1, 2));
		this.m22 = floatBuffer.get(bufferIndex(2, 2));
		this.m23 = floatBuffer.get(bufferIndex(3, 2));
		this.m30 = floatBuffer.get(bufferIndex(0, 3));
		this.m31 = floatBuffer.get(bufferIndex(1, 3));
		this.m32 = floatBuffer.get(bufferIndex(2, 3));
		this.m33 = floatBuffer.get(bufferIndex(3, 3));
	}

	public void load(FloatBuffer floatBuffer, boolean bl) {
		if (bl) {
			this.loadTransposed(floatBuffer);
		} else {
			this.load(floatBuffer);
		}
	}

	public void load(Matrix4f matrix4f) {
		this.m00 = matrix4f.m00;
		this.m01 = matrix4f.m01;
		this.m02 = matrix4f.m02;
		this.m03 = matrix4f.m03;
		this.m10 = matrix4f.m10;
		this.m11 = matrix4f.m11;
		this.m12 = matrix4f.m12;
		this.m13 = matrix4f.m13;
		this.m20 = matrix4f.m20;
		this.m21 = matrix4f.m21;
		this.m22 = matrix4f.m22;
		this.m23 = matrix4f.m23;
		this.m30 = matrix4f.m30;
		this.m31 = matrix4f.m31;
		this.m32 = matrix4f.m32;
		this.m33 = matrix4f.m33;
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Matrix4f:\n");
		stringBuilder.append(this.m00);
		stringBuilder.append(" ");
		stringBuilder.append(this.m01);
		stringBuilder.append(" ");
		stringBuilder.append(this.m02);
		stringBuilder.append(" ");
		stringBuilder.append(this.m03);
		stringBuilder.append("\n");
		stringBuilder.append(this.m10);
		stringBuilder.append(" ");
		stringBuilder.append(this.m11);
		stringBuilder.append(" ");
		stringBuilder.append(this.m12);
		stringBuilder.append(" ");
		stringBuilder.append(this.m13);
		stringBuilder.append("\n");
		stringBuilder.append(this.m20);
		stringBuilder.append(" ");
		stringBuilder.append(this.m21);
		stringBuilder.append(" ");
		stringBuilder.append(this.m22);
		stringBuilder.append(" ");
		stringBuilder.append(this.m23);
		stringBuilder.append("\n");
		stringBuilder.append(this.m30);
		stringBuilder.append(" ");
		stringBuilder.append(this.m31);
		stringBuilder.append(" ");
		stringBuilder.append(this.m32);
		stringBuilder.append(" ");
		stringBuilder.append(this.m33);
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}

	public void store(FloatBuffer floatBuffer) {
		floatBuffer.put(bufferIndex(0, 0), this.m00);
		floatBuffer.put(bufferIndex(0, 1), this.m01);
		floatBuffer.put(bufferIndex(0, 2), this.m02);
		floatBuffer.put(bufferIndex(0, 3), this.m03);
		floatBuffer.put(bufferIndex(1, 0), this.m10);
		floatBuffer.put(bufferIndex(1, 1), this.m11);
		floatBuffer.put(bufferIndex(1, 2), this.m12);
		floatBuffer.put(bufferIndex(1, 3), this.m13);
		floatBuffer.put(bufferIndex(2, 0), this.m20);
		floatBuffer.put(bufferIndex(2, 1), this.m21);
		floatBuffer.put(bufferIndex(2, 2), this.m22);
		floatBuffer.put(bufferIndex(2, 3), this.m23);
		floatBuffer.put(bufferIndex(3, 0), this.m30);
		floatBuffer.put(bufferIndex(3, 1), this.m31);
		floatBuffer.put(bufferIndex(3, 2), this.m32);
		floatBuffer.put(bufferIndex(3, 3), this.m33);
	}

	public void storeTransposed(FloatBuffer floatBuffer) {
		floatBuffer.put(bufferIndex(0, 0), this.m00);
		floatBuffer.put(bufferIndex(1, 0), this.m01);
		floatBuffer.put(bufferIndex(2, 0), this.m02);
		floatBuffer.put(bufferIndex(3, 0), this.m03);
		floatBuffer.put(bufferIndex(0, 1), this.m10);
		floatBuffer.put(bufferIndex(1, 1), this.m11);
		floatBuffer.put(bufferIndex(2, 1), this.m12);
		floatBuffer.put(bufferIndex(3, 1), this.m13);
		floatBuffer.put(bufferIndex(0, 2), this.m20);
		floatBuffer.put(bufferIndex(1, 2), this.m21);
		floatBuffer.put(bufferIndex(2, 2), this.m22);
		floatBuffer.put(bufferIndex(3, 2), this.m23);
		floatBuffer.put(bufferIndex(0, 3), this.m30);
		floatBuffer.put(bufferIndex(1, 3), this.m31);
		floatBuffer.put(bufferIndex(2, 3), this.m32);
		floatBuffer.put(bufferIndex(3, 3), this.m33);
	}

	public void store(FloatBuffer floatBuffer, boolean bl) {
		if (bl) {
			this.storeTransposed(floatBuffer);
		} else {
			this.store(floatBuffer);
		}
	}

	public void setIdentity() {
		this.m00 = 1.0F;
		this.m01 = 0.0F;
		this.m02 = 0.0F;
		this.m03 = 0.0F;
		this.m10 = 0.0F;
		this.m11 = 1.0F;
		this.m12 = 0.0F;
		this.m13 = 0.0F;
		this.m20 = 0.0F;
		this.m21 = 0.0F;
		this.m22 = 1.0F;
		this.m23 = 0.0F;
		this.m30 = 0.0F;
		this.m31 = 0.0F;
		this.m32 = 0.0F;
		this.m33 = 1.0F;
	}

	public float adjugateAndDet() {
		float f = this.m00 * this.m11 - this.m01 * this.m10;
		float g = this.m00 * this.m12 - this.m02 * this.m10;
		float h = this.m00 * this.m13 - this.m03 * this.m10;
		float i = this.m01 * this.m12 - this.m02 * this.m11;
		float j = this.m01 * this.m13 - this.m03 * this.m11;
		float k = this.m02 * this.m13 - this.m03 * this.m12;
		float l = this.m20 * this.m31 - this.m21 * this.m30;
		float m = this.m20 * this.m32 - this.m22 * this.m30;
		float n = this.m20 * this.m33 - this.m23 * this.m30;
		float o = this.m21 * this.m32 - this.m22 * this.m31;
		float p = this.m21 * this.m33 - this.m23 * this.m31;
		float q = this.m22 * this.m33 - this.m23 * this.m32;
		float r = this.m11 * q - this.m12 * p + this.m13 * o;
		float s = -this.m10 * q + this.m12 * n - this.m13 * m;
		float t = this.m10 * p - this.m11 * n + this.m13 * l;
		float u = -this.m10 * o + this.m11 * m - this.m12 * l;
		float v = -this.m01 * q + this.m02 * p - this.m03 * o;
		float w = this.m00 * q - this.m02 * n + this.m03 * m;
		float x = -this.m00 * p + this.m01 * n - this.m03 * l;
		float y = this.m00 * o - this.m01 * m + this.m02 * l;
		float z = this.m31 * k - this.m32 * j + this.m33 * i;
		float aa = -this.m30 * k + this.m32 * h - this.m33 * g;
		float ab = this.m30 * j - this.m31 * h + this.m33 * f;
		float ac = -this.m30 * i + this.m31 * g - this.m32 * f;
		float ad = -this.m21 * k + this.m22 * j - this.m23 * i;
		float ae = this.m20 * k - this.m22 * h + this.m23 * g;
		float af = -this.m20 * j + this.m21 * h - this.m23 * f;
		float ag = this.m20 * i - this.m21 * g + this.m22 * f;
		this.m00 = r;
		this.m10 = s;
		this.m20 = t;
		this.m30 = u;
		this.m01 = v;
		this.m11 = w;
		this.m21 = x;
		this.m31 = y;
		this.m02 = z;
		this.m12 = aa;
		this.m22 = ab;
		this.m32 = ac;
		this.m03 = ad;
		this.m13 = ae;
		this.m23 = af;
		this.m33 = ag;
		return f * q - g * p + h * o + i * n - j * m + k * l;
	}

	public float determinant() {
		float f = this.m00 * this.m11 - this.m01 * this.m10;
		float g = this.m00 * this.m12 - this.m02 * this.m10;
		float h = this.m00 * this.m13 - this.m03 * this.m10;
		float i = this.m01 * this.m12 - this.m02 * this.m11;
		float j = this.m01 * this.m13 - this.m03 * this.m11;
		float k = this.m02 * this.m13 - this.m03 * this.m12;
		float l = this.m20 * this.m31 - this.m21 * this.m30;
		float m = this.m20 * this.m32 - this.m22 * this.m30;
		float n = this.m20 * this.m33 - this.m23 * this.m30;
		float o = this.m21 * this.m32 - this.m22 * this.m31;
		float p = this.m21 * this.m33 - this.m23 * this.m31;
		float q = this.m22 * this.m33 - this.m23 * this.m32;
		return f * q - g * p + h * o + i * n - j * m + k * l;
	}

	public void transpose() {
		float f = this.m10;
		this.m10 = this.m01;
		this.m01 = f;
		f = this.m20;
		this.m20 = this.m02;
		this.m02 = f;
		f = this.m21;
		this.m21 = this.m12;
		this.m12 = f;
		f = this.m30;
		this.m30 = this.m03;
		this.m03 = f;
		f = this.m31;
		this.m31 = this.m13;
		this.m13 = f;
		f = this.m32;
		this.m32 = this.m23;
		this.m23 = f;
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

	public void multiply(Matrix4f matrix4f) {
		float f = this.m00 * matrix4f.m00 + this.m01 * matrix4f.m10 + this.m02 * matrix4f.m20 + this.m03 * matrix4f.m30;
		float g = this.m00 * matrix4f.m01 + this.m01 * matrix4f.m11 + this.m02 * matrix4f.m21 + this.m03 * matrix4f.m31;
		float h = this.m00 * matrix4f.m02 + this.m01 * matrix4f.m12 + this.m02 * matrix4f.m22 + this.m03 * matrix4f.m32;
		float i = this.m00 * matrix4f.m03 + this.m01 * matrix4f.m13 + this.m02 * matrix4f.m23 + this.m03 * matrix4f.m33;
		float j = this.m10 * matrix4f.m00 + this.m11 * matrix4f.m10 + this.m12 * matrix4f.m20 + this.m13 * matrix4f.m30;
		float k = this.m10 * matrix4f.m01 + this.m11 * matrix4f.m11 + this.m12 * matrix4f.m21 + this.m13 * matrix4f.m31;
		float l = this.m10 * matrix4f.m02 + this.m11 * matrix4f.m12 + this.m12 * matrix4f.m22 + this.m13 * matrix4f.m32;
		float m = this.m10 * matrix4f.m03 + this.m11 * matrix4f.m13 + this.m12 * matrix4f.m23 + this.m13 * matrix4f.m33;
		float n = this.m20 * matrix4f.m00 + this.m21 * matrix4f.m10 + this.m22 * matrix4f.m20 + this.m23 * matrix4f.m30;
		float o = this.m20 * matrix4f.m01 + this.m21 * matrix4f.m11 + this.m22 * matrix4f.m21 + this.m23 * matrix4f.m31;
		float p = this.m20 * matrix4f.m02 + this.m21 * matrix4f.m12 + this.m22 * matrix4f.m22 + this.m23 * matrix4f.m32;
		float q = this.m20 * matrix4f.m03 + this.m21 * matrix4f.m13 + this.m22 * matrix4f.m23 + this.m23 * matrix4f.m33;
		float r = this.m30 * matrix4f.m00 + this.m31 * matrix4f.m10 + this.m32 * matrix4f.m20 + this.m33 * matrix4f.m30;
		float s = this.m30 * matrix4f.m01 + this.m31 * matrix4f.m11 + this.m32 * matrix4f.m21 + this.m33 * matrix4f.m31;
		float t = this.m30 * matrix4f.m02 + this.m31 * matrix4f.m12 + this.m32 * matrix4f.m22 + this.m33 * matrix4f.m32;
		float u = this.m30 * matrix4f.m03 + this.m31 * matrix4f.m13 + this.m32 * matrix4f.m23 + this.m33 * matrix4f.m33;
		this.m00 = f;
		this.m01 = g;
		this.m02 = h;
		this.m03 = i;
		this.m10 = j;
		this.m11 = k;
		this.m12 = l;
		this.m13 = m;
		this.m20 = n;
		this.m21 = o;
		this.m22 = p;
		this.m23 = q;
		this.m30 = r;
		this.m31 = s;
		this.m32 = t;
		this.m33 = u;
	}

	public void multiply(Quaternion quaternion) {
		this.multiply(new Matrix4f(quaternion));
	}

	public void multiply(float f) {
		this.m00 *= f;
		this.m01 *= f;
		this.m02 *= f;
		this.m03 *= f;
		this.m10 *= f;
		this.m11 *= f;
		this.m12 *= f;
		this.m13 *= f;
		this.m20 *= f;
		this.m21 *= f;
		this.m22 *= f;
		this.m23 *= f;
		this.m30 *= f;
		this.m31 *= f;
		this.m32 *= f;
		this.m33 *= f;
	}

	public void add(Matrix4f matrix4f) {
		this.m00 = this.m00 + matrix4f.m00;
		this.m01 = this.m01 + matrix4f.m01;
		this.m02 = this.m02 + matrix4f.m02;
		this.m03 = this.m03 + matrix4f.m03;
		this.m10 = this.m10 + matrix4f.m10;
		this.m11 = this.m11 + matrix4f.m11;
		this.m12 = this.m12 + matrix4f.m12;
		this.m13 = this.m13 + matrix4f.m13;
		this.m20 = this.m20 + matrix4f.m20;
		this.m21 = this.m21 + matrix4f.m21;
		this.m22 = this.m22 + matrix4f.m22;
		this.m23 = this.m23 + matrix4f.m23;
		this.m30 = this.m30 + matrix4f.m30;
		this.m31 = this.m31 + matrix4f.m31;
		this.m32 = this.m32 + matrix4f.m32;
		this.m33 = this.m33 + matrix4f.m33;
	}

	public void subtract(Matrix4f matrix4f) {
		this.m00 = this.m00 - matrix4f.m00;
		this.m01 = this.m01 - matrix4f.m01;
		this.m02 = this.m02 - matrix4f.m02;
		this.m03 = this.m03 - matrix4f.m03;
		this.m10 = this.m10 - matrix4f.m10;
		this.m11 = this.m11 - matrix4f.m11;
		this.m12 = this.m12 - matrix4f.m12;
		this.m13 = this.m13 - matrix4f.m13;
		this.m20 = this.m20 - matrix4f.m20;
		this.m21 = this.m21 - matrix4f.m21;
		this.m22 = this.m22 - matrix4f.m22;
		this.m23 = this.m23 - matrix4f.m23;
		this.m30 = this.m30 - matrix4f.m30;
		this.m31 = this.m31 - matrix4f.m31;
		this.m32 = this.m32 - matrix4f.m32;
		this.m33 = this.m33 - matrix4f.m33;
	}

	public float trace() {
		return this.m00 + this.m11 + this.m22 + this.m33;
	}

	public static Matrix4f perspective(double d, float f, float g, float h) {
		float i = (float)(1.0 / Math.tan(d * (float) (Math.PI / 180.0) / 2.0));
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.m00 = i / f;
		matrix4f.m11 = i;
		matrix4f.m22 = (h + g) / (g - h);
		matrix4f.m32 = -1.0F;
		matrix4f.m23 = 2.0F * h * g / (g - h);
		return matrix4f;
	}

	public static Matrix4f orthographic(float f, float g, float h, float i) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.m00 = 2.0F / f;
		matrix4f.m11 = 2.0F / g;
		float j = i - h;
		matrix4f.m22 = -2.0F / j;
		matrix4f.m33 = 1.0F;
		matrix4f.m03 = -1.0F;
		matrix4f.m13 = 1.0F;
		matrix4f.m23 = -(i + h) / j;
		return matrix4f;
	}

	public static Matrix4f orthographic(float f, float g, float h, float i, float j, float k) {
		Matrix4f matrix4f = new Matrix4f();
		float l = g - f;
		float m = h - i;
		float n = k - j;
		matrix4f.m00 = 2.0F / l;
		matrix4f.m11 = 2.0F / m;
		matrix4f.m22 = -2.0F / n;
		matrix4f.m03 = -(g + f) / l;
		matrix4f.m13 = -(h + i) / m;
		matrix4f.m23 = -(k + j) / n;
		matrix4f.m33 = 1.0F;
		return matrix4f;
	}

	public void translate(Vector3f vector3f) {
		this.m03 = this.m03 + vector3f.x();
		this.m13 = this.m13 + vector3f.y();
		this.m23 = this.m23 + vector3f.z();
	}

	public Matrix4f copy() {
		return new Matrix4f(this);
	}

	public void multiplyWithTranslation(float f, float g, float h) {
		this.m03 = this.m00 * f + this.m01 * g + this.m02 * h + this.m03;
		this.m13 = this.m10 * f + this.m11 * g + this.m12 * h + this.m13;
		this.m23 = this.m20 * f + this.m21 * g + this.m22 * h + this.m23;
		this.m33 = this.m30 * f + this.m31 * g + this.m32 * h + this.m33;
	}

	public static Matrix4f createScaleMatrix(float f, float g, float h) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.m00 = f;
		matrix4f.m11 = g;
		matrix4f.m22 = h;
		matrix4f.m33 = 1.0F;
		return matrix4f;
	}

	public static Matrix4f createTranslateMatrix(float f, float g, float h) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.m00 = 1.0F;
		matrix4f.m11 = 1.0F;
		matrix4f.m22 = 1.0F;
		matrix4f.m33 = 1.0F;
		matrix4f.m03 = f;
		matrix4f.m13 = g;
		matrix4f.m23 = h;
		return matrix4f;
	}
}
