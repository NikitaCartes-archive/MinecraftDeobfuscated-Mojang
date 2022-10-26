/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
    private static final float G = 3.0f + 2.0f * (float)Math.sqrt(2.0);
    private static final float CS = (float)Math.cos(0.39269908169872414);
    private static final float SS = (float)Math.sin(0.39269908169872414);

    private MatrixUtil() {
    }

    public static Matrix4f mulComponentWise(Matrix4f matrix4f, float f) {
        return matrix4f.set(matrix4f.m00() * f, matrix4f.m01() * f, matrix4f.m02() * f, matrix4f.m03() * f, matrix4f.m10() * f, matrix4f.m11() * f, matrix4f.m12() * f, matrix4f.m13() * f, matrix4f.m20() * f, matrix4f.m21() * f, matrix4f.m22() * f, matrix4f.m23() * f, matrix4f.m30() * f, matrix4f.m31() * f, matrix4f.m32() * f, matrix4f.m33() * f);
    }

    private static Pair<Float, Float> approxGivensQuat(float f, float g, float h) {
        float j = g;
        float i = 2.0f * (f - h);
        if (G * j * j < i * i) {
            float k = Mth.fastInvSqrt(j * j + i * i);
            return Pair.of(Float.valueOf(k * j), Float.valueOf(k * i));
        }
        return Pair.of(Float.valueOf(SS), Float.valueOf(CS));
    }

    private static Pair<Float, Float> qrGivensQuat(float f, float g) {
        float k;
        float h = (float)Math.hypot(f, g);
        float i = h > 1.0E-6f ? g : 0.0f;
        float j = Math.abs(f) + Math.max(h, 1.0E-6f);
        if (f < 0.0f) {
            k = i;
            i = j;
            j = k;
        }
        k = Mth.fastInvSqrt(j * j + i * i);
        return Pair.of(Float.valueOf(i *= k), Float.valueOf(j *= k));
    }

    private static Quaternionf stepJacobi(Matrix3f matrix3f) {
        float h;
        float g;
        float f;
        Quaternionf quaternionf2;
        Float float2;
        Float float_;
        Pair<Float, Float> pair;
        Matrix3f matrix3f2 = new Matrix3f();
        Quaternionf quaternionf = new Quaternionf();
        if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6f) {
            pair = MatrixUtil.approxGivensQuat(matrix3f.m00, 0.5f * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
            float_ = pair.getFirst();
            float2 = pair.getSecond();
            quaternionf2 = new Quaternionf(0.0f, 0.0f, float_.floatValue(), float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
            g = -2.0f * float_.floatValue() * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
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
        if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6f) {
            pair = MatrixUtil.approxGivensQuat(matrix3f.m00, 0.5f * (matrix3f.m02 + matrix3f.m20), matrix3f.m22);
            float i = -pair.getFirst().floatValue();
            float2 = pair.getSecond();
            quaternionf2 = new Quaternionf(0.0f, i, 0.0f, float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - i * i;
            g = -2.0f * i * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + i * i;
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
        if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6f) {
            pair = MatrixUtil.approxGivensQuat(matrix3f.m11, 0.5f * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
            float_ = pair.getFirst();
            float2 = pair.getSecond();
            quaternionf2 = new Quaternionf(float_.floatValue(), 0.0f, 0.0f, float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
            g = -2.0f * float_.floatValue() * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
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
        for (int i = 0; i < 5; ++i) {
            quaternionf2.mul(MatrixUtil.stepJacobi(matrix3f2));
        }
        quaternionf2.normalize();
        Matrix3f matrix3f3 = new Matrix3f(matrix3f);
        matrix3f3.rotate(quaternionf2);
        float f = 1.0f;
        Pair<Float, Float> pair = MatrixUtil.qrGivensQuat(matrix3f3.m00, matrix3f3.m01);
        Float float_ = pair.getFirst();
        Float float2 = pair.getSecond();
        float g = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
        float h = -2.0f * float_.floatValue() * float2.floatValue();
        float j = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
        Quaternionf quaternionf3 = new Quaternionf(0.0f, 0.0f, float_.floatValue(), float2.floatValue());
        quaternionf.mul(quaternionf3);
        Matrix3f matrix3f4 = new Matrix3f();
        matrix3f4.m00 = g;
        matrix3f4.m11 = g;
        matrix3f4.m01 = h;
        matrix3f4.m10 = -h;
        matrix3f4.m22 = j;
        f *= j;
        matrix3f4.mul(matrix3f3);
        pair = MatrixUtil.qrGivensQuat(matrix3f4.m00, matrix3f4.m02);
        float k = -pair.getFirst().floatValue();
        Float float3 = pair.getSecond();
        float l = float3.floatValue() * float3.floatValue() - k * k;
        float m = -2.0f * k * float3.floatValue();
        float n = float3.floatValue() * float3.floatValue() + k * k;
        Quaternionf quaternionf4 = new Quaternionf(0.0f, k, 0.0f, float3.floatValue());
        quaternionf.mul(quaternionf4);
        Matrix3f matrix3f5 = new Matrix3f();
        matrix3f5.m00 = l;
        matrix3f5.m22 = l;
        matrix3f5.m02 = -m;
        matrix3f5.m20 = m;
        matrix3f5.m11 = n;
        f *= n;
        matrix3f5.mul(matrix3f4);
        pair = MatrixUtil.qrGivensQuat(matrix3f5.m11, matrix3f5.m12);
        Float float4 = pair.getFirst();
        Float float5 = pair.getSecond();
        float o = float5.floatValue() * float5.floatValue() - float4.floatValue() * float4.floatValue();
        float p = -2.0f * float4.floatValue() * float5.floatValue();
        float q = float5.floatValue() * float5.floatValue() + float4.floatValue() * float4.floatValue();
        Quaternionf quaternionf5 = new Quaternionf(float4.floatValue(), 0.0f, 0.0f, float5.floatValue());
        quaternionf.mul(quaternionf5);
        Matrix3f matrix3f6 = new Matrix3f();
        matrix3f6.m11 = o;
        matrix3f6.m22 = o;
        matrix3f6.m12 = p;
        matrix3f6.m21 = -p;
        matrix3f6.m00 = q;
        f *= q;
        matrix3f6.mul(matrix3f5);
        f = 1.0f / f;
        quaternionf.mul((float)Math.sqrt(f));
        Vector3f vector3f = new Vector3f(matrix3f6.m00 * f, matrix3f6.m11 * f, matrix3f6.m22 * f);
        return Triple.of(quaternionf, vector3f, quaternionf2);
    }

    public static Matrix4x3f toAffine(Matrix4f matrix4f) {
        float f = 1.0f / matrix4f.m33();
        return new Matrix4x3f().set(matrix4f).scaleLocal(f, f, f);
    }
}

