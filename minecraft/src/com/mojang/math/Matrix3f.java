package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;

public final class Matrix3f {
	private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0);
	private static final float CS = (float)Math.cos(Math.PI / 8);
	private static final float SS = (float)Math.sin(Math.PI / 8);
	private static final float SQ2 = 1.0F / (float)Math.sqrt(2.0);
	protected float m00;
	protected float m01;
	protected float m02;
	protected float m10;
	protected float m11;
	protected float m12;
	protected float m20;
	protected float m21;
	protected float m22;

	public Matrix3f() {
	}

	public Matrix3f(Quaternion quaternion) {
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

	@Environment(EnvType.CLIENT)
	public static Matrix3f createScaleMatrix(float f, float g, float h) {
		Matrix3f matrix3f = new Matrix3f();
		matrix3f.m00 = f;
		matrix3f.m11 = g;
		matrix3f.m22 = h;
		return matrix3f;
	}

	public Matrix3f(Matrix4f matrix4f) {
		this.m00 = matrix4f.m00;
		this.m01 = matrix4f.m01;
		this.m02 = matrix4f.m02;
		this.m10 = matrix4f.m10;
		this.m11 = matrix4f.m11;
		this.m12 = matrix4f.m12;
		this.m20 = matrix4f.m20;
		this.m21 = matrix4f.m21;
		this.m22 = matrix4f.m22;
	}

	public Matrix3f(Matrix3f matrix3f) {
		this.m00 = matrix3f.m00;
		this.m01 = matrix3f.m01;
		this.m02 = matrix3f.m02;
		this.m10 = matrix3f.m10;
		this.m11 = matrix3f.m11;
		this.m12 = matrix3f.m12;
		this.m20 = matrix3f.m20;
		this.m21 = matrix3f.m21;
		this.m22 = matrix3f.m22;
	}

	@Environment(EnvType.CLIENT)
	private static Pair<Float, Float> approxGivensQuat(float f, float g, float h) {
		float i = 2.0F * (f - h);
		if (G * g * g < i * i) {
			float k = Mth.fastInvSqrt(g * g + i * i);
			return Pair.of(k * g, k * i);
		} else {
			return Pair.of(SS, CS);
		}
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	private static Quaternion stepJacobi(Matrix3f matrix3f) {
		Matrix3f matrix3f2 = new Matrix3f();
		Quaternion quaternion = Quaternion.ONE.copy();
		if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.m00, 0.5F * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
			Float float_ = pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternion quaternion2 = new Quaternion(0.0F, 0.0F, float_, float2);
			float f = float2 * float2 - float_ * float_;
			float g = -2.0F * float_ * float2;
			float h = float2 * float2 + float_ * float_;
			quaternion.mul(quaternion2);
			matrix3f2.setIdentity();
			matrix3f2.m00 = f;
			matrix3f2.m11 = f;
			matrix3f2.m10 = -g;
			matrix3f2.m01 = g;
			matrix3f2.m22 = h;
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.load(matrix3f2);
		}

		if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.m00, 0.5F * (matrix3f.m02 + matrix3f.m20), matrix3f.m22);
			float i = -pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternion quaternion2 = new Quaternion(0.0F, i, 0.0F, float2);
			float f = float2 * float2 - i * i;
			float g = -2.0F * i * float2;
			float h = float2 * float2 + i * i;
			quaternion.mul(quaternion2);
			matrix3f2.setIdentity();
			matrix3f2.m00 = f;
			matrix3f2.m22 = f;
			matrix3f2.m20 = g;
			matrix3f2.m02 = -g;
			matrix3f2.m11 = h;
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.load(matrix3f2);
		}

		if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.m11, 0.5F * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
			Float float_ = pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternion quaternion2 = new Quaternion(float_, 0.0F, 0.0F, float2);
			float f = float2 * float2 - float_ * float_;
			float g = -2.0F * float_ * float2;
			float h = float2 * float2 + float_ * float_;
			quaternion.mul(quaternion2);
			matrix3f2.setIdentity();
			matrix3f2.m11 = f;
			matrix3f2.m22 = f;
			matrix3f2.m21 = -g;
			matrix3f2.m12 = g;
			matrix3f2.m00 = h;
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.load(matrix3f2);
		}

		return quaternion;
	}

	@Environment(EnvType.CLIENT)
	public void transpose() {
		float f = this.m01;
		this.m01 = this.m10;
		this.m10 = f;
		f = this.m02;
		this.m02 = this.m20;
		this.m20 = f;
		f = this.m12;
		this.m12 = this.m21;
		this.m21 = f;
	}

	@Environment(EnvType.CLIENT)
	public Triple<Quaternion, Vector3f, Quaternion> svdDecompose() {
		Quaternion quaternion = Quaternion.ONE.copy();
		Quaternion quaternion2 = Quaternion.ONE.copy();
		Matrix3f matrix3f = this.copy();
		matrix3f.transpose();
		matrix3f.mul(this);

		for (int i = 0; i < 5; i++) {
			quaternion2.mul(stepJacobi(matrix3f));
		}

		quaternion2.normalize();
		Matrix3f matrix3f2 = new Matrix3f(this);
		matrix3f2.mul(new Matrix3f(quaternion2));
		float f = 1.0F;
		Pair<Float, Float> pair = qrGivensQuat(matrix3f2.m00, matrix3f2.m10);
		Float float_ = pair.getFirst();
		Float float2 = pair.getSecond();
		float g = float2 * float2 - float_ * float_;
		float h = -2.0F * float_ * float2;
		float j = float2 * float2 + float_ * float_;
		Quaternion quaternion3 = new Quaternion(0.0F, 0.0F, float_, float2);
		quaternion.mul(quaternion3);
		Matrix3f matrix3f3 = new Matrix3f();
		matrix3f3.setIdentity();
		matrix3f3.m00 = g;
		matrix3f3.m11 = g;
		matrix3f3.m10 = h;
		matrix3f3.m01 = -h;
		matrix3f3.m22 = j;
		f *= j;
		matrix3f3.mul(matrix3f2);
		pair = qrGivensQuat(matrix3f3.m00, matrix3f3.m20);
		float k = -pair.getFirst();
		Float float3 = pair.getSecond();
		float l = float3 * float3 - k * k;
		float m = -2.0F * k * float3;
		float n = float3 * float3 + k * k;
		Quaternion quaternion4 = new Quaternion(0.0F, k, 0.0F, float3);
		quaternion.mul(quaternion4);
		Matrix3f matrix3f4 = new Matrix3f();
		matrix3f4.setIdentity();
		matrix3f4.m00 = l;
		matrix3f4.m22 = l;
		matrix3f4.m20 = -m;
		matrix3f4.m02 = m;
		matrix3f4.m11 = n;
		f *= n;
		matrix3f4.mul(matrix3f3);
		pair = qrGivensQuat(matrix3f4.m11, matrix3f4.m21);
		Float float4 = pair.getFirst();
		Float float5 = pair.getSecond();
		float o = float5 * float5 - float4 * float4;
		float p = -2.0F * float4 * float5;
		float q = float5 * float5 + float4 * float4;
		Quaternion quaternion5 = new Quaternion(float4, 0.0F, 0.0F, float5);
		quaternion.mul(quaternion5);
		Matrix3f matrix3f5 = new Matrix3f();
		matrix3f5.setIdentity();
		matrix3f5.m11 = o;
		matrix3f5.m22 = o;
		matrix3f5.m21 = p;
		matrix3f5.m12 = -p;
		matrix3f5.m00 = q;
		f *= q;
		matrix3f5.mul(matrix3f4);
		f = 1.0F / f;
		quaternion.mul((float)Math.sqrt((double)f));
		Vector3f vector3f = new Vector3f(matrix3f5.m00 * f, matrix3f5.m11 * f, matrix3f5.m22 * f);
		return Triple.of(quaternion, vector3f, quaternion2);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Matrix3f matrix3f = (Matrix3f)object;
			return Float.compare(matrix3f.m00, this.m00) == 0
				&& Float.compare(matrix3f.m01, this.m01) == 0
				&& Float.compare(matrix3f.m02, this.m02) == 0
				&& Float.compare(matrix3f.m10, this.m10) == 0
				&& Float.compare(matrix3f.m11, this.m11) == 0
				&& Float.compare(matrix3f.m12, this.m12) == 0
				&& Float.compare(matrix3f.m20, this.m20) == 0
				&& Float.compare(matrix3f.m21, this.m21) == 0
				&& Float.compare(matrix3f.m22, this.m22) == 0;
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
		i = 31 * i + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
		i = 31 * i + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
		i = 31 * i + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
		i = 31 * i + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
		i = 31 * i + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
		i = 31 * i + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
		i = 31 * i + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
		return 31 * i + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
	}

	@Environment(EnvType.CLIENT)
	public void load(Matrix3f matrix3f) {
		this.m00 = matrix3f.m00;
		this.m01 = matrix3f.m01;
		this.m02 = matrix3f.m02;
		this.m10 = matrix3f.m10;
		this.m11 = matrix3f.m11;
		this.m12 = matrix3f.m12;
		this.m20 = matrix3f.m20;
		this.m21 = matrix3f.m21;
		this.m22 = matrix3f.m22;
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Matrix3f:\n");
		stringBuilder.append(this.m00);
		stringBuilder.append(" ");
		stringBuilder.append(this.m01);
		stringBuilder.append(" ");
		stringBuilder.append(this.m02);
		stringBuilder.append("\n");
		stringBuilder.append(this.m10);
		stringBuilder.append(" ");
		stringBuilder.append(this.m11);
		stringBuilder.append(" ");
		stringBuilder.append(this.m12);
		stringBuilder.append("\n");
		stringBuilder.append(this.m20);
		stringBuilder.append(" ");
		stringBuilder.append(this.m21);
		stringBuilder.append(" ");
		stringBuilder.append(this.m22);
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}

	@Environment(EnvType.CLIENT)
	public void setIdentity() {
		this.m00 = 1.0F;
		this.m01 = 0.0F;
		this.m02 = 0.0F;
		this.m10 = 0.0F;
		this.m11 = 1.0F;
		this.m12 = 0.0F;
		this.m20 = 0.0F;
		this.m21 = 0.0F;
		this.m22 = 1.0F;
	}

	@Environment(EnvType.CLIENT)
	public float adjugateAndDet() {
		float f = this.m11 * this.m22 - this.m12 * this.m21;
		float g = -(this.m10 * this.m22 - this.m12 * this.m20);
		float h = this.m10 * this.m21 - this.m11 * this.m20;
		float i = -(this.m01 * this.m22 - this.m02 * this.m21);
		float j = this.m00 * this.m22 - this.m02 * this.m20;
		float k = -(this.m00 * this.m21 - this.m01 * this.m20);
		float l = this.m01 * this.m12 - this.m02 * this.m11;
		float m = -(this.m00 * this.m12 - this.m02 * this.m10);
		float n = this.m00 * this.m11 - this.m01 * this.m10;
		float o = this.m00 * f + this.m01 * g + this.m02 * h;
		this.m00 = f;
		this.m10 = g;
		this.m20 = h;
		this.m01 = i;
		this.m11 = j;
		this.m21 = k;
		this.m02 = l;
		this.m12 = m;
		this.m22 = n;
		return o;
	}

	@Environment(EnvType.CLIENT)
	public boolean invert() {
		float f = this.adjugateAndDet();
		if (Math.abs(f) > 1.0E-6F) {
			this.mul(f);
			return true;
		} else {
			return false;
		}
	}

	public void set(int i, int j, float f) {
		if (i == 0) {
			if (j == 0) {
				this.m00 = f;
			} else if (j == 1) {
				this.m01 = f;
			} else {
				this.m02 = f;
			}
		} else if (i == 1) {
			if (j == 0) {
				this.m10 = f;
			} else if (j == 1) {
				this.m11 = f;
			} else {
				this.m12 = f;
			}
		} else if (j == 0) {
			this.m20 = f;
		} else if (j == 1) {
			this.m21 = f;
		} else {
			this.m22 = f;
		}
	}

	public void mul(Matrix3f matrix3f) {
		float f = this.m00 * matrix3f.m00 + this.m01 * matrix3f.m10 + this.m02 * matrix3f.m20;
		float g = this.m00 * matrix3f.m01 + this.m01 * matrix3f.m11 + this.m02 * matrix3f.m21;
		float h = this.m00 * matrix3f.m02 + this.m01 * matrix3f.m12 + this.m02 * matrix3f.m22;
		float i = this.m10 * matrix3f.m00 + this.m11 * matrix3f.m10 + this.m12 * matrix3f.m20;
		float j = this.m10 * matrix3f.m01 + this.m11 * matrix3f.m11 + this.m12 * matrix3f.m21;
		float k = this.m10 * matrix3f.m02 + this.m11 * matrix3f.m12 + this.m12 * matrix3f.m22;
		float l = this.m20 * matrix3f.m00 + this.m21 * matrix3f.m10 + this.m22 * matrix3f.m20;
		float m = this.m20 * matrix3f.m01 + this.m21 * matrix3f.m11 + this.m22 * matrix3f.m21;
		float n = this.m20 * matrix3f.m02 + this.m21 * matrix3f.m12 + this.m22 * matrix3f.m22;
		this.m00 = f;
		this.m01 = g;
		this.m02 = h;
		this.m10 = i;
		this.m11 = j;
		this.m12 = k;
		this.m20 = l;
		this.m21 = m;
		this.m22 = n;
	}

	@Environment(EnvType.CLIENT)
	public void mul(Quaternion quaternion) {
		this.mul(new Matrix3f(quaternion));
	}

	@Environment(EnvType.CLIENT)
	public void mul(float f) {
		this.m00 *= f;
		this.m01 *= f;
		this.m02 *= f;
		this.m10 *= f;
		this.m11 *= f;
		this.m12 *= f;
		this.m20 *= f;
		this.m21 *= f;
		this.m22 *= f;
	}

	@Environment(EnvType.CLIENT)
	public Matrix3f copy() {
		return new Matrix3f(this);
	}
}
