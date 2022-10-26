package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
	private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0);
	private static final float CS = (float)Math.cos(Math.PI / 8);
	private static final float SS = (float)Math.sin(Math.PI / 8);

	private MatrixUtil() {
	}

	public static Matrix4f mulComponentWise(Matrix4f matrix4f, float f) {
		return matrix4f.set(
			matrix4f.m00() * f,
			matrix4f.m01() * f,
			matrix4f.m02() * f,
			matrix4f.m03() * f,
			matrix4f.m10() * f,
			matrix4f.m11() * f,
			matrix4f.m12() * f,
			matrix4f.m13() * f,
			matrix4f.m20() * f,
			matrix4f.m21() * f,
			matrix4f.m22() * f,
			matrix4f.m23() * f,
			matrix4f.m30() * f,
			matrix4f.m31() * f,
			matrix4f.m32() * f,
			matrix4f.m33() * f
		);
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

	private static Quaternionf stepJacobi(Matrix3f matrix3f) {
		Matrix3f matrix3f2 = new Matrix3f();
		Quaternionf quaternionf = new Quaternionf();
		if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.m00, 0.5F * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
			Float float_ = pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternionf quaternionf2 = new Quaternionf(0.0F, 0.0F, float_, float2);
			float f = float2 * float2 - float_ * float_;
			float g = -2.0F * float_ * float2;
			float h = float2 * float2 + float_ * float_;
			quaternionf.mul(quaternionf2);
			matrix3f2.m00 = f;
			matrix3f2.m11 = f;
			matrix3f2.m01 = -g;
			matrix3f2.m10 = g;
			matrix3f2.m22 = h;
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.set(matrix3f2);
		}

		if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.m00, 0.5F * (matrix3f.m02 + matrix3f.m20), matrix3f.m22);
			float i = -pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternionf quaternionf2 = new Quaternionf(0.0F, i, 0.0F, float2);
			float f = float2 * float2 - i * i;
			float g = -2.0F * i * float2;
			float h = float2 * float2 + i * i;
			quaternionf.mul(quaternionf2);
			matrix3f2.m00 = f;
			matrix3f2.m22 = f;
			matrix3f2.m02 = g;
			matrix3f2.m20 = -g;
			matrix3f2.m11 = h;
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.set(matrix3f2);
		}

		if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6F) {
			Pair<Float, Float> pair = approxGivensQuat(matrix3f.m11, 0.5F * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
			Float float_ = pair.getFirst();
			Float float2 = pair.getSecond();
			Quaternionf quaternionf2 = new Quaternionf(float_, 0.0F, 0.0F, float2);
			float f = float2 * float2 - float_ * float_;
			float g = -2.0F * float_ * float2;
			float h = float2 * float2 + float_ * float_;
			quaternionf.mul(quaternionf2);
			matrix3f2.m11 = f;
			matrix3f2.m22 = f;
			matrix3f2.m12 = -g;
			matrix3f2.m21 = g;
			matrix3f2.m00 = h;
			matrix3f.mul(matrix3f2);
			matrix3f2.transpose();
			matrix3f2.mul(matrix3f);
			matrix3f.set(matrix3f2);
		}

		return quaternionf;
	}

	public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f matrix3f) {
		Quaternionf quaternionf = new Quaternionf();
		Quaternionf quaternionf2 = new Quaternionf();
		Matrix3f matrix3f2 = new Matrix3f(matrix3f);
		matrix3f2.transpose();
		matrix3f2.mul(matrix3f);

		for (int i = 0; i < 5; i++) {
			quaternionf2.mul(stepJacobi(matrix3f2));
		}

		quaternionf2.normalize();
		Matrix3f matrix3f3 = new Matrix3f(matrix3f);
		matrix3f3.rotate(quaternionf2);
		float f = 1.0F;
		Pair<Float, Float> pair = qrGivensQuat(matrix3f3.m00, matrix3f3.m01);
		Float float_ = pair.getFirst();
		Float float2 = pair.getSecond();
		float g = float2 * float2 - float_ * float_;
		float h = -2.0F * float_ * float2;
		float j = float2 * float2 + float_ * float_;
		Quaternionf quaternionf3 = new Quaternionf(0.0F, 0.0F, float_, float2);
		quaternionf.mul(quaternionf3);
		Matrix3f matrix3f4 = new Matrix3f();
		matrix3f4.m00 = g;
		matrix3f4.m11 = g;
		matrix3f4.m01 = h;
		matrix3f4.m10 = -h;
		matrix3f4.m22 = j;
		f *= j;
		matrix3f4.mul(matrix3f3);
		pair = qrGivensQuat(matrix3f4.m00, matrix3f4.m02);
		float k = -pair.getFirst();
		Float float3 = pair.getSecond();
		float l = float3 * float3 - k * k;
		float m = -2.0F * k * float3;
		float n = float3 * float3 + k * k;
		Quaternionf quaternionf4 = new Quaternionf(0.0F, k, 0.0F, float3);
		quaternionf.mul(quaternionf4);
		Matrix3f matrix3f5 = new Matrix3f();
		matrix3f5.m00 = l;
		matrix3f5.m22 = l;
		matrix3f5.m02 = -m;
		matrix3f5.m20 = m;
		matrix3f5.m11 = n;
		f *= n;
		matrix3f5.mul(matrix3f4);
		pair = qrGivensQuat(matrix3f5.m11, matrix3f5.m12);
		Float float4 = pair.getFirst();
		Float float5 = pair.getSecond();
		float o = float5 * float5 - float4 * float4;
		float p = -2.0F * float4 * float5;
		float q = float5 * float5 + float4 * float4;
		Quaternionf quaternionf5 = new Quaternionf(float4, 0.0F, 0.0F, float5);
		quaternionf.mul(quaternionf5);
		Matrix3f matrix3f6 = new Matrix3f();
		matrix3f6.m11 = o;
		matrix3f6.m22 = o;
		matrix3f6.m12 = p;
		matrix3f6.m21 = -p;
		matrix3f6.m00 = q;
		f *= q;
		matrix3f6.mul(matrix3f5);
		f = 1.0F / f;
		quaternionf.mul((float)Math.sqrt((double)f));
		Vector3f vector3f = new Vector3f(matrix3f6.m00 * f, matrix3f6.m11 * f, matrix3f6.m22 * f);
		return Triple.of(quaternionf, vector3f, quaternionf2);
	}

	public static Matrix4x3f toAffine(Matrix4f matrix4f) {
		float f = 1.0F / matrix4f.m33();
		return new Matrix4x3f().set(matrix4f).scaleLocal(f, f, f);
	}
}
