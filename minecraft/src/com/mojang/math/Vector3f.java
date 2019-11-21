package com.mojang.math;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Vector3f {
	public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
	public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
	public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
	public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
	public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
	public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
	private float x;
	private float y;
	private float z;

	public Vector3f() {
	}

	public Vector3f(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
	}

	public Vector3f(Vec3 vec3) {
		this((float)vec3.x, (float)vec3.y, (float)vec3.z);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Vector3f vector3f = (Vector3f)object;
			if (Float.compare(vector3f.x, this.x) != 0) {
				return false;
			} else {
				return Float.compare(vector3f.y, this.y) != 0 ? false : Float.compare(vector3f.z, this.z) == 0;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = Float.floatToIntBits(this.x);
		i = 31 * i + Float.floatToIntBits(this.y);
		return 31 * i + Float.floatToIntBits(this.z);
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

	@Environment(EnvType.CLIENT)
	public void mul(float f) {
		this.x *= f;
		this.y *= f;
		this.z *= f;
	}

	@Environment(EnvType.CLIENT)
	public void mul(float f, float g, float h) {
		this.x *= f;
		this.y *= g;
		this.z *= h;
	}

	@Environment(EnvType.CLIENT)
	public void clamp(float f, float g) {
		this.x = Mth.clamp(this.x, f, g);
		this.y = Mth.clamp(this.y, f, g);
		this.z = Mth.clamp(this.z, f, g);
	}

	public void set(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
	}

	@Environment(EnvType.CLIENT)
	public void add(float f, float g, float h) {
		this.x += f;
		this.y += g;
		this.z += h;
	}

	@Environment(EnvType.CLIENT)
	public void add(Vector3f vector3f) {
		this.x = this.x + vector3f.x;
		this.y = this.y + vector3f.y;
		this.z = this.z + vector3f.z;
	}

	@Environment(EnvType.CLIENT)
	public void sub(Vector3f vector3f) {
		this.x = this.x - vector3f.x;
		this.y = this.y - vector3f.y;
		this.z = this.z - vector3f.z;
	}

	@Environment(EnvType.CLIENT)
	public float dot(Vector3f vector3f) {
		return this.x * vector3f.x + this.y * vector3f.y + this.z * vector3f.z;
	}

	@Environment(EnvType.CLIENT)
	public boolean normalize() {
		float f = this.x * this.x + this.y * this.y + this.z * this.z;
		if ((double)f < 1.0E-5) {
			return false;
		} else {
			float g = Mth.fastInvSqrt(f);
			this.x *= g;
			this.y *= g;
			this.z *= g;
			return true;
		}
	}

	@Environment(EnvType.CLIENT)
	public void cross(Vector3f vector3f) {
		float f = this.x;
		float g = this.y;
		float h = this.z;
		float i = vector3f.x();
		float j = vector3f.y();
		float k = vector3f.z();
		this.x = g * k - h * j;
		this.y = h * i - f * k;
		this.z = f * j - g * i;
	}

	@Environment(EnvType.CLIENT)
	public void transform(Matrix3f matrix3f) {
		float f = this.x;
		float g = this.y;
		float h = this.z;
		this.x = matrix3f.m00 * f + matrix3f.m01 * g + matrix3f.m02 * h;
		this.y = matrix3f.m10 * f + matrix3f.m11 * g + matrix3f.m12 * h;
		this.z = matrix3f.m20 * f + matrix3f.m21 * g + matrix3f.m22 * h;
	}

	public void transform(Quaternion quaternion) {
		Quaternion quaternion2 = new Quaternion(quaternion);
		quaternion2.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
		Quaternion quaternion3 = new Quaternion(quaternion);
		quaternion3.conj();
		quaternion2.mul(quaternion3);
		this.set(quaternion2.i(), quaternion2.j(), quaternion2.k());
	}

	@Environment(EnvType.CLIENT)
	public void lerp(Vector3f vector3f, float f) {
		float g = 1.0F - f;
		this.x = this.x * g + vector3f.x * f;
		this.y = this.y * g + vector3f.y * f;
		this.z = this.z * g + vector3f.z * f;
	}

	@Environment(EnvType.CLIENT)
	public Quaternion rotation(float f) {
		return new Quaternion(this, f, false);
	}

	@Environment(EnvType.CLIENT)
	public Quaternion rotationDegrees(float f) {
		return new Quaternion(this, f, true);
	}

	@Environment(EnvType.CLIENT)
	public Vector3f copy() {
		return new Vector3f(this.x, this.y, this.z);
	}

	@Environment(EnvType.CLIENT)
	public void map(Float2FloatFunction float2FloatFunction) {
		this.x = float2FloatFunction.get(this.x);
		this.y = float2FloatFunction.get(this.y);
		this.z = float2FloatFunction.get(this.z);
	}

	public String toString() {
		return "[" + this.x + ", " + this.y + ", " + this.z + "]";
	}
}
