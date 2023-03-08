/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.math.GivensParameters;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
    private static final float G = 3.0f + 2.0f * Math.sqrt(2.0f);
    private static final GivensParameters PI_4 = GivensParameters.fromPositiveAngle(0.7853982f);

    private MatrixUtil() {
    }

    public static Matrix4f mulComponentWise(Matrix4f matrix4f, float f) {
        return matrix4f.set(matrix4f.m00() * f, matrix4f.m01() * f, matrix4f.m02() * f, matrix4f.m03() * f, matrix4f.m10() * f, matrix4f.m11() * f, matrix4f.m12() * f, matrix4f.m13() * f, matrix4f.m20() * f, matrix4f.m21() * f, matrix4f.m22() * f, matrix4f.m23() * f, matrix4f.m30() * f, matrix4f.m31() * f, matrix4f.m32() * f, matrix4f.m33() * f);
    }

    private static GivensParameters approxGivensQuat(float f, float g, float h) {
        float j = g;
        float i = 2.0f * (f - h);
        if (G * j * j < i * i) {
            return GivensParameters.fromUnnormalized(j, i);
        }
        return PI_4;
    }

    private static GivensParameters qrGivensQuat(float f, float g) {
        float h = (float)java.lang.Math.hypot(f, g);
        float i = h > 1.0E-6f ? g : 0.0f;
        float j = Math.abs(f) + Math.max(h, 1.0E-6f);
        if (f < 0.0f) {
            float k = i;
            i = j;
            j = k;
        }
        return GivensParameters.fromUnnormalized(i, j);
    }

    private static void similarityTransform(Matrix3f matrix3f, Matrix3f matrix3f2) {
        matrix3f.mul(matrix3f2);
        matrix3f2.transpose();
        matrix3f2.mul(matrix3f);
        matrix3f.set(matrix3f2);
    }

    private static void stepJacobi(Matrix3f matrix3f, Matrix3f matrix3f2, Quaternionf quaternionf, Quaternionf quaternionf2) {
        Quaternionf quaternionf3;
        GivensParameters givensParameters;
        if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6f) {
            givensParameters = MatrixUtil.approxGivensQuat(matrix3f.m00, 0.5f * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
            quaternionf3 = givensParameters.aroundZ(quaternionf);
            quaternionf2.mul(quaternionf3);
            givensParameters.aroundZ(matrix3f2);
            MatrixUtil.similarityTransform(matrix3f, matrix3f2);
        }
        if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6f) {
            givensParameters = MatrixUtil.approxGivensQuat(matrix3f.m00, 0.5f * (matrix3f.m02 + matrix3f.m20), matrix3f.m22).inverse();
            quaternionf3 = givensParameters.aroundY(quaternionf);
            quaternionf2.mul(quaternionf3);
            givensParameters.aroundY(matrix3f2);
            MatrixUtil.similarityTransform(matrix3f, matrix3f2);
        }
        if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6f) {
            givensParameters = MatrixUtil.approxGivensQuat(matrix3f.m11, 0.5f * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
            quaternionf3 = givensParameters.aroundX(quaternionf);
            quaternionf2.mul(quaternionf3);
            givensParameters.aroundX(matrix3f2);
            MatrixUtil.similarityTransform(matrix3f, matrix3f2);
        }
    }

    public static Quaternionf eigenvalueJacobi(Matrix3f matrix3f, int i) {
        Quaternionf quaternionf = new Quaternionf();
        Matrix3f matrix3f2 = new Matrix3f();
        Quaternionf quaternionf2 = new Quaternionf();
        for (int j = 0; j < i; ++j) {
            MatrixUtil.stepJacobi(matrix3f, matrix3f2, quaternionf2, quaternionf);
        }
        quaternionf.normalize();
        return quaternionf;
    }

    public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f matrix3f) {
        Matrix3f matrix3f2 = new Matrix3f(matrix3f);
        matrix3f2.transpose();
        matrix3f2.mul(matrix3f);
        Quaternionf quaternionf = MatrixUtil.eigenvalueJacobi(matrix3f2, 5);
        boolean bl = (double)matrix3f2.m00 < 1.0E-6;
        boolean bl2 = (double)matrix3f2.m11 < 1.0E-6;
        Matrix3f matrix3f3 = matrix3f2;
        Matrix3f matrix3f4 = matrix3f.rotate(quaternionf);
        float f = 1.0f;
        Quaternionf quaternionf2 = new Quaternionf();
        Quaternionf quaternionf3 = new Quaternionf();
        GivensParameters givensParameters = bl ? MatrixUtil.qrGivensQuat(matrix3f4.m11, -matrix3f4.m10) : MatrixUtil.qrGivensQuat(matrix3f4.m00, matrix3f4.m01);
        Quaternionf quaternionf4 = givensParameters.aroundZ(quaternionf3);
        Matrix3f matrix3f5 = givensParameters.aroundZ(matrix3f3);
        f *= matrix3f5.m22;
        quaternionf2.mul(quaternionf4);
        matrix3f5.transpose().mul(matrix3f4);
        matrix3f3 = matrix3f4;
        givensParameters = bl ? MatrixUtil.qrGivensQuat(matrix3f5.m22, -matrix3f5.m20) : MatrixUtil.qrGivensQuat(matrix3f5.m00, matrix3f5.m02);
        givensParameters = givensParameters.inverse();
        Quaternionf quaternionf5 = givensParameters.aroundY(quaternionf3);
        Matrix3f matrix3f6 = givensParameters.aroundY(matrix3f3);
        f *= matrix3f6.m11;
        quaternionf2.mul(quaternionf5);
        matrix3f6.transpose().mul(matrix3f5);
        matrix3f3 = matrix3f5;
        givensParameters = bl2 ? MatrixUtil.qrGivensQuat(matrix3f6.m22, -matrix3f6.m21) : MatrixUtil.qrGivensQuat(matrix3f6.m11, matrix3f6.m12);
        Quaternionf quaternionf6 = givensParameters.aroundX(quaternionf3);
        Matrix3f matrix3f7 = givensParameters.aroundX(matrix3f3);
        f *= matrix3f7.m00;
        quaternionf2.mul(quaternionf6);
        matrix3f7.transpose().mul(matrix3f6);
        f = 1.0f / f;
        quaternionf2.mul(Math.sqrt(f));
        Vector3f vector3f = new Vector3f(matrix3f7.m00 * f, matrix3f7.m11 * f, matrix3f7.m22 * f);
        return Triple.of(quaternionf2, vector3f, quaternionf.conjugate());
    }
}

