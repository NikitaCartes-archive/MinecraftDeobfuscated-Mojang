package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;

@Environment(EnvType.CLIENT)
public final class Matrix3f {
	private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0);
	private static final float CS = (float)Math.cos(Math.PI / 8);
	private static final float SS = (float)Math.sin(Math.PI / 8);
	private static final float SQ2 = 1.0F / (float)Math.sqrt(2.0);
	private final float[] values;

	public Matrix3f() {
		this.values = new float[9];
	}

	public Matrix3f(Quaternion quaternion) {
		this();
		float f = quaternion.i();
		float g = quaternion.j();
		float h = quaternion.k();
		float i = quaternion.r();
		float j = 2.0F * f * f;
		float k = 2.0F * g * g;
		float l = 2.0F * h * h;
		this.values[0] = 1.0F - k - l;
		this.values[4] = 1.0F - l - j;
		this.values[8] = 1.0F - j - k;
		float m = f * g;
		float n = g * h;
		float o = h * f;
		float p = f * i;
		float q = g * i;
		float r = h * i;
		this.values[1] = 2.0F * (m + r);
		this.values[3] = 2.0F * (m - r);
		this.values[2] = 2.0F * (o - q);
		this.values[6] = 2.0F * (o + q);
		this.values[5] = 2.0F * (n + p);
		this.values[7] = 2.0F * (n - p);
	}

	public Matrix3f(Matrix3f matrix3f, boolean bl) {
		this(matrix3f.values, true);
	}

	public Matrix3f(Matrix4f matrix4f) {
		this();

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				this.values[j + i * 3] = matrix4f.get(j, i);
			}
		}
	}

	public Matrix3f(float[] fs, boolean bl) {
		if (bl) {
			this.values = new float[9];

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					this.values[j + i * 3] = fs[i + j * 3];
				}
			}
		} else {
			this.values = Arrays.copyOf(fs, fs.length);
		}
	}

	public Matrix3f(Matrix3f matrix3f) {
		this.values = Arrays.copyOf(matrix3f.values, 9);
	}

	private static Pair<Float, Float> approxGivensQuat(float f, float g, float h) {
		float i = 2.0F * (f - h);
		if (G * g * g < i * i) {
			float k = Mth.fastInvSqrt(g * g + i * i);
			return Pair.of(k * g, k * i);
		} else {
			return Pair.of(SS, CS);
		}
	}

	private static Pair<Float, Float> qrGivensQuat(float f, float g) {
		float h = (float)Math.hypot((double)f, (double)g);
		float i = h > 1.0E-6F ? g : 0.0F;
		float j = Math.abs(f) + Math.max(h, 1.0E-6F);
		if (f < 0.0F) {
			float k = i;
			i = j;
			j = k;
		}

		float k = Mth.fastInvSqrt(j * j + i * i);
		j *= k;
		i *= k;
		return Pair.of(i, j);
	}

	private static Quaternion stepJacobi(Matrix3f matrix3f) {
		Matrix3f matrix3f2 = new Matrix3f();
		Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
		if (matrix3f.get(0, 1) * matrix3f.get(0, 1) + matrix3f.get(1, 0) * matrix3f.get(1, 0) > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.get(0, 0), 0.5F * (matrix3f.get(0, 1) + matrix3f.get(1, 0)), matrix3f.get(1, 1));
			Float float_ = pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternion quaternion2 = new Quaternion(0.0F, 0.0F, float_, float2);
			float f = float2 * float2 - float_ * float_;
			float g = -2.0F * float_ * float2;
			float h = float2 * float2 + float_ * float_;
			quaternion.mul(quaternion2);
			matrix3f2.setIdentity();
			matrix3f2.set(0, 0, f);
			matrix3f2.set(1, 1, f);
			matrix3f2.set(1, 0, -g);
			matrix3f2.set(0, 1, g);
			matrix3f2.set(2, 2, h);
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.load(matrix3f2);
		}

		if (matrix3f.get(0, 2) * matrix3f.get(0, 2) + matrix3f.get(2, 0) * matrix3f.get(2, 0) > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.get(0, 0), 0.5F * (matrix3f.get(0, 2) + matrix3f.get(2, 0)), matrix3f.get(2, 2));
			float i = -pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternion quaternion2 = new Quaternion(0.0F, i, 0.0F, float2);
			float f = float2 * float2 - i * i;
			float g = -2.0F * i * float2;
			float h = float2 * float2 + i * i;
			quaternion.mul(quaternion2);
			matrix3f2.setIdentity();
			matrix3f2.set(0, 0, f);
			matrix3f2.set(2, 2, f);
			matrix3f2.set(2, 0, g);
			matrix3f2.set(0, 2, -g);
			matrix3f2.set(1, 1, h);
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.load(matrix3f2);
		}

		if (matrix3f.get(1, 2) * matrix3f.get(1, 2) + matrix3f.get(2, 1) * matrix3f.get(2, 1) > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.get(1, 1), 0.5F * (matrix3f.get(1, 2) + matrix3f.get(2, 1)), matrix3f.get(2, 2));
			Float float_ = pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternion quaternion2 = new Quaternion(float_, 0.0F, 0.0F, float2);
			float f = float2 * float2 - float_ * float_;
			float g = -2.0F * float_ * float2;
			float h = float2 * float2 + float_ * float_;
			quaternion.mul(quaternion2);
			matrix3f2.setIdentity();
			matrix3f2.set(1, 1, f);
			matrix3f2.set(2, 2, f);
			matrix3f2.set(2, 1, -g);
			matrix3f2.set(1, 2, g);
			matrix3f2.set(0, 0, h);
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.load(matrix3f2);
		}

		return quaternion;
	}

	public void transpose() {
		this.swap(0, 1);
		this.swap(0, 2);
		this.swap(1, 2);
	}

	private void swap(int i, int j) {
		float f = this.values[i + 3 * j];
		this.values[i + 3 * j] = this.values[j + 3 * i];
		this.values[j + 3 * i] = f;
	}

	public Triple<Quaternion, Vector3f, Quaternion> svdDecompose() {
		Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
		Quaternion quaternion2 = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
		Matrix3f matrix3f = new Matrix3f(this, true);
		matrix3f.mul(this);

		for (int i = 0; i < 5; i++) {
			quaternion2.mul(stepJacobi(matrix3f));
		}

		quaternion2.normalize();
		Matrix3f matrix3f2 = new Matrix3f(this);
		matrix3f2.mul(new Matrix3f(quaternion2));
		float f = 1.0F;
		Pair<Float, Float> pair = qrGivensQuat(matrix3f2.get(0, 0), matrix3f2.get(1, 0));
		Float float_ = pair.getFirst();
		Float float2 = pair.getSecond();
		float g = float2 * float2 - float_ * float_;
		float h = -2.0F * float_ * float2;
		float j = float2 * float2 + float_ * float_;
		Quaternion quaternion3 = new Quaternion(0.0F, 0.0F, float_, float2);
		quaternion.mul(quaternion3);
		Matrix3f matrix3f3 = new Matrix3f();
		matrix3f3.setIdentity();
		matrix3f3.set(0, 0, g);
		matrix3f3.set(1, 1, g);
		matrix3f3.set(1, 0, h);
		matrix3f3.set(0, 1, -h);
		matrix3f3.set(2, 2, j);
		f *= j;
		matrix3f3.mul(matrix3f2);
		pair = qrGivensQuat(matrix3f3.get(0, 0), matrix3f3.get(2, 0));
		float k = -pair.getFirst();
		Float float3 = pair.getSecond();
		float l = float3 * float3 - k * k;
		float m = -2.0F * k * float3;
		float n = float3 * float3 + k * k;
		Quaternion quaternion4 = new Quaternion(0.0F, k, 0.0F, float3);
		quaternion.mul(quaternion4);
		Matrix3f matrix3f4 = new Matrix3f();
		matrix3f4.setIdentity();
		matrix3f4.set(0, 0, l);
		matrix3f4.set(2, 2, l);
		matrix3f4.set(2, 0, -m);
		matrix3f4.set(0, 2, m);
		matrix3f4.set(1, 1, n);
		f *= n;
		matrix3f4.mul(matrix3f3);
		pair = qrGivensQuat(matrix3f4.get(1, 1), matrix3f4.get(2, 1));
		Float float4 = pair.getFirst();
		Float float5 = pair.getSecond();
		float o = float5 * float5 - float4 * float4;
		float p = -2.0F * float4 * float5;
		float q = float5 * float5 + float4 * float4;
		Quaternion quaternion5 = new Quaternion(float4, 0.0F, 0.0F, float5);
		quaternion.mul(quaternion5);
		Matrix3f matrix3f5 = new Matrix3f();
		matrix3f5.setIdentity();
		matrix3f5.set(1, 1, o);
		matrix3f5.set(2, 2, o);
		matrix3f5.set(2, 1, p);
		matrix3f5.set(1, 2, -p);
		matrix3f5.set(0, 0, q);
		f *= q;
		matrix3f5.mul(matrix3f4);
		f = 1.0F / f;
		quaternion.mul((float)Math.sqrt((double)f));
		Vector3f vector3f = new Vector3f(matrix3f5.get(0, 0) * f, matrix3f5.get(1, 1) * f, matrix3f5.get(2, 2) * f);
		return Triple.of(quaternion, vector3f, quaternion2);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Matrix3f matrix3f = (Matrix3f)object;
			return Arrays.equals(this.values, matrix3f.values);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Arrays.hashCode(this.values);
	}

	public void load(Matrix3f matrix3f) {
		System.arraycopy(matrix3f.values, 0, this.values, 0, 9);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Matrix3f:\n");

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				stringBuilder.append(this.values[i + j * 3]);
				if (j != 2) {
					stringBuilder.append(" ");
				}
			}

			stringBuilder.append("\n");
		}

		return stringBuilder.toString();
	}

	public void setIdentity() {
		this.values[0] = 1.0F;
		this.values[1] = 0.0F;
		this.values[2] = 0.0F;
		this.values[3] = 0.0F;
		this.values[4] = 1.0F;
		this.values[5] = 0.0F;
		this.values[6] = 0.0F;
		this.values[7] = 0.0F;
		this.values[8] = 1.0F;
	}

	public float get(int i, int j) {
		return this.values[3 * j + i];
	}

	public void set(int i, int j, float f) {
		this.values[3 * j + i] = f;
	}

	public void mul(Matrix3f matrix3f) {
		float[] fs = Arrays.copyOf(this.values, 9);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				this.values[i + j * 3] = 0.0F;

				for (int k = 0; k < 3; k++) {
					this.values[i + j * 3] = this.values[i + j * 3] + fs[i + k * 3] * matrix3f.values[k + j * 3];
				}
			}
		}
	}
}
