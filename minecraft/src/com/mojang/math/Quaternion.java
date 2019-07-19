package com.mojang.math;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public final class Quaternion {
	private final float[] values;

	public Quaternion() {
		this.values = new float[4];
		this.values[4] = 1.0F;
	}

	public Quaternion(float f, float g, float h, float i) {
		this.values = new float[4];
		this.values[0] = f;
		this.values[1] = g;
		this.values[2] = h;
		this.values[3] = i;
	}

	public Quaternion(Vector3f vector3f, float f, boolean bl) {
		if (bl) {
			f *= (float) (Math.PI / 180.0);
		}

		float g = sin(f / 2.0F);
		this.values = new float[4];
		this.values[0] = vector3f.x() * g;
		this.values[1] = vector3f.y() * g;
		this.values[2] = vector3f.z() * g;
		this.values[3] = cos(f / 2.0F);
	}

	@Environment(EnvType.CLIENT)
	public Quaternion(float f, float g, float h, boolean bl) {
		if (bl) {
			f *= (float) (Math.PI / 180.0);
			g *= (float) (Math.PI / 180.0);
			h *= (float) (Math.PI / 180.0);
		}

		float i = sin(0.5F * f);
		float j = cos(0.5F * f);
		float k = sin(0.5F * g);
		float l = cos(0.5F * g);
		float m = sin(0.5F * h);
		float n = cos(0.5F * h);
		this.values = new float[4];
		this.values[0] = i * l * n + j * k * m;
		this.values[1] = j * k * n - i * l * m;
		this.values[2] = i * k * n + j * l * m;
		this.values[3] = j * l * n - i * k * m;
	}

	public Quaternion(Quaternion quaternion) {
		this.values = Arrays.copyOf(quaternion.values, 4);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Quaternion quaternion = (Quaternion)object;
			return Arrays.equals(this.values, quaternion.values);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Arrays.hashCode(this.values);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Quaternion[").append(this.r()).append(" + ");
		stringBuilder.append(this.i()).append("i + ");
		stringBuilder.append(this.j()).append("j + ");
		stringBuilder.append(this.k()).append("k]");
		return stringBuilder.toString();
	}

	public float i() {
		return this.values[0];
	}

	public float j() {
		return this.values[1];
	}

	public float k() {
		return this.values[2];
	}

	public float r() {
		return this.values[3];
	}

	public void mul(Quaternion quaternion) {
		float f = this.i();
		float g = this.j();
		float h = this.k();
		float i = this.r();
		float j = quaternion.i();
		float k = quaternion.j();
		float l = quaternion.k();
		float m = quaternion.r();
		this.values[0] = i * j + f * m + g * l - h * k;
		this.values[1] = i * k - f * l + g * m + h * j;
		this.values[2] = i * l + f * k - g * j + h * m;
		this.values[3] = i * m - f * j - g * k - h * l;
	}

	public void conj() {
		this.values[0] = -this.values[0];
		this.values[1] = -this.values[1];
		this.values[2] = -this.values[2];
	}

	private static float cos(float f) {
		return (float)Math.cos((double)f);
	}

	private static float sin(float f) {
		return (float)Math.sin((double)f);
	}
}
