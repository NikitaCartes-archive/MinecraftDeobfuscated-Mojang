package com.mojang.math;

import java.util.Arrays;
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
	private final float[] values;

	@Environment(EnvType.CLIENT)
	public Vector3f(Vector3f vector3f) {
		this.values = Arrays.copyOf(vector3f.values, 3);
	}

	public Vector3f() {
		this.values = new float[3];
	}

	public Vector3f(float f, float g, float h) {
		this.values = new float[]{f, g, h};
	}

	public Vector3f(Vec3 vec3) {
		this.values = new float[]{(float)vec3.x, (float)vec3.y, (float)vec3.z};
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Vector3f vector3f = (Vector3f)object;
			return Arrays.equals(this.values, vector3f.values);
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

	@Environment(EnvType.CLIENT)
	public void mul(float f) {
		for (int i = 0; i < 3; i++) {
			this.values[i] = this.values[i] * f;
		}
	}

	@Environment(EnvType.CLIENT)
	private static float clamp(float f, float g, float h) {
		if (f < g) {
			return g;
		} else {
			return f > h ? h : f;
		}
	}

	@Environment(EnvType.CLIENT)
	public void clamp(float f, float g) {
		this.values[0] = clamp(this.values[0], f, g);
		this.values[1] = clamp(this.values[1], f, g);
		this.values[2] = clamp(this.values[2], f, g);
	}

	public void set(float f, float g, float h) {
		this.values[0] = f;
		this.values[1] = g;
		this.values[2] = h;
	}

	@Environment(EnvType.CLIENT)
	public void add(float f, float g, float h) {
		this.values[0] = this.values[0] + f;
		this.values[1] = this.values[1] + g;
		this.values[2] = this.values[2] + h;
	}

	@Environment(EnvType.CLIENT)
	public void sub(Vector3f vector3f) {
		for (int i = 0; i < 3; i++) {
			this.values[i] = this.values[i] - vector3f.values[i];
		}
	}

	@Environment(EnvType.CLIENT)
	public float dot(Vector3f vector3f) {
		float f = 0.0F;

		for (int i = 0; i < 3; i++) {
			f += this.values[i] * vector3f.values[i];
		}

		return f;
	}

	@Environment(EnvType.CLIENT)
	public void normalize() {
		float f = 0.0F;

		for (int i = 0; i < 3; i++) {
			f += this.values[i] * this.values[i];
		}

		float g = (float)Mth.fastInvSqrt((double)f);

		for (int j = 0; j < 3; j++) {
			this.values[j] = this.values[j] * g;
		}
	}

	@Environment(EnvType.CLIENT)
	public void cross(Vector3f vector3f) {
		float f = this.values[0];
		float g = this.values[1];
		float h = this.values[2];
		float i = vector3f.x();
		float j = vector3f.y();
		float k = vector3f.z();
		this.values[0] = g * k - h * j;
		this.values[1] = h * i - f * k;
		this.values[2] = f * j - g * i;
	}

	public void transform(Quaternion quaternion) {
		Quaternion quaternion2 = new Quaternion(quaternion);
		quaternion2.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
		Quaternion quaternion3 = new Quaternion(quaternion);
		quaternion3.conj();
		quaternion2.mul(quaternion3);
		this.set(quaternion2.i(), quaternion2.j(), quaternion2.k());
	}
}
