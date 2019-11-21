package com.mojang.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Vector4f {
	private float x;
	private float y;
	private float z;
	private float w;

	public Vector4f() {
	}

	public Vector4f(float f, float g, float h, float i) {
		this.x = f;
		this.y = g;
		this.z = h;
		this.w = i;
	}

	public Vector4f(Vector3f vector3f) {
		this(vector3f.x(), vector3f.y(), vector3f.z(), 1.0F);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Vector4f vector4f = (Vector4f)object;
			if (Float.compare(vector4f.x, this.x) != 0) {
				return false;
			} else if (Float.compare(vector4f.y, this.y) != 0) {
				return false;
			} else {
				return Float.compare(vector4f.z, this.z) != 0 ? false : Float.compare(vector4f.w, this.w) == 0;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = Float.floatToIntBits(this.x);
		i = 31 * i + Float.floatToIntBits(this.y);
		i = 31 * i + Float.floatToIntBits(this.z);
		return 31 * i + Float.floatToIntBits(this.w);
	}

	public float x() {
		return this.x;
	}

	public float y() {
		return this.y;
	}

	public float z() {
		return this.z;
	}

	public float w() {
		return this.w;
	}

	public void mul(Vector3f vector3f) {
		this.x = this.x * vector3f.x();
		this.y = this.y * vector3f.y();
		this.z = this.z * vector3f.z();
	}

	public void set(float f, float g, float h, float i) {
		this.x = f;
		this.y = g;
		this.z = h;
		this.w = i;
	}

	public float dot(Vector4f vector4f) {
		return this.x * vector4f.x + this.y * vector4f.y + this.z * vector4f.z + this.w * vector4f.w;
	}

	public boolean normalize() {
		float f = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
		if ((double)f < 1.0E-5) {
			return false;
		} else {
			float g = Mth.fastInvSqrt(f);
			this.x *= g;
			this.y *= g;
			this.z *= g;
			this.w *= g;
			return true;
		}
	}

	public void transform(Matrix4f matrix4f) {
		float f = this.x;
		float g = this.y;
		float h = this.z;
		float i = this.w;
		this.x = matrix4f.m00 * f + matrix4f.m01 * g + matrix4f.m02 * h + matrix4f.m03 * i;
		this.y = matrix4f.m10 * f + matrix4f.m11 * g + matrix4f.m12 * h + matrix4f.m13 * i;
		this.z = matrix4f.m20 * f + matrix4f.m21 * g + matrix4f.m22 * h + matrix4f.m23 * i;
		this.w = matrix4f.m30 * f + matrix4f.m31 * g + matrix4f.m32 * h + matrix4f.m33 * i;
	}

	public void transform(Quaternion quaternion) {
		Quaternion quaternion2 = new Quaternion(quaternion);
		quaternion2.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
		Quaternion quaternion3 = new Quaternion(quaternion);
		quaternion3.conj();
		quaternion2.mul(quaternion3);
		this.set(quaternion2.i(), quaternion2.j(), quaternion2.k(), this.w());
	}

	public void perspectiveDivide() {
		this.x = this.x / this.w;
		this.y = this.y / this.w;
		this.z = this.z / this.w;
		this.w = 1.0F;
	}

	public String toString() {
		return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
	}
}
