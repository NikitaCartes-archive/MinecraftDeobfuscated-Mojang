/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;

@Environment(value=EnvType.CLIENT)
public final class Matrix3f {
    private static final float G = 3.0f + 2.0f * (float)Math.sqrt(2.0);
    private static final float CS = (float)Math.cos(0.39269908169872414);
    private static final float SS = (float)Math.sin(0.39269908169872414);
    private static final float SQ2 = 1.0f / (float)Math.sqrt(2.0);
    private final float[] values;

    public Matrix3f() {
        this(new float[9]);
    }

    private Matrix3f(float[] fs) {
        this.values = fs;
    }

    public Matrix3f(Quaternion quaternion) {
        this();
        float f = quaternion.i();
        float g = quaternion.j();
        float h = quaternion.k();
        float i = quaternion.r();
        float j = 2.0f * f * f;
        float k = 2.0f * g * g;
        float l = 2.0f * h * h;
        this.set(0, 0, 1.0f - k - l);
        this.set(1, 1, 1.0f - l - j);
        this.set(2, 2, 1.0f - j - k);
        float m = f * g;
        float n = g * h;
        float o = h * f;
        float p = f * i;
        float q = g * i;
        float r = h * i;
        this.set(1, 0, 2.0f * (m + r));
        this.set(0, 1, 2.0f * (m - r));
        this.set(2, 0, 2.0f * (o - q));
        this.set(0, 2, 2.0f * (o + q));
        this.set(2, 1, 2.0f * (n + p));
        this.set(1, 2, 2.0f * (n - p));
    }

    public Matrix3f(Matrix3f matrix3f, boolean bl) {
        this(matrix3f.values, true);
    }

    public Matrix3f(Matrix4f matrix4f) {
        this();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.values[j + i * 3] = matrix4f.get(j, i);
            }
        }
    }

    public Matrix3f(float[] fs, boolean bl) {
        this(bl ? new float[9] : Arrays.copyOf(fs, fs.length));
        if (bl) {
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    this.values[j + i * 3] = fs[i + j * 3];
                }
            }
        }
    }

    public Matrix3f(Matrix3f matrix3f) {
        this(Arrays.copyOf(matrix3f.values, 9));
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

    private static Quaternion stepJacobi(Matrix3f matrix3f) {
        float h;
        float g;
        float f;
        Quaternion quaternion2;
        Float float2;
        Float float_;
        Pair<Float, Float> pair;
        Matrix3f matrix3f2 = new Matrix3f();
        Quaternion quaternion = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
        if (matrix3f.get(0, 1) * matrix3f.get(0, 1) + matrix3f.get(1, 0) * matrix3f.get(1, 0) > 1.0E-6f) {
            pair = Matrix3f.approxGivensQuat(matrix3f.get(0, 0), 0.5f * (matrix3f.get(0, 1) + matrix3f.get(1, 0)), matrix3f.get(1, 1));
            float_ = pair.getFirst();
            float2 = pair.getSecond();
            quaternion2 = new Quaternion(0.0f, 0.0f, float_.floatValue(), float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
            g = -2.0f * float_.floatValue() * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
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
        if (matrix3f.get(0, 2) * matrix3f.get(0, 2) + matrix3f.get(2, 0) * matrix3f.get(2, 0) > 1.0E-6f) {
            pair = Matrix3f.approxGivensQuat(matrix3f.get(0, 0), 0.5f * (matrix3f.get(0, 2) + matrix3f.get(2, 0)), matrix3f.get(2, 2));
            float i = -pair.getFirst().floatValue();
            float2 = pair.getSecond();
            quaternion2 = new Quaternion(0.0f, i, 0.0f, float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - i * i;
            g = -2.0f * i * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + i * i;
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
        if (matrix3f.get(1, 2) * matrix3f.get(1, 2) + matrix3f.get(2, 1) * matrix3f.get(2, 1) > 1.0E-6f) {
            pair = Matrix3f.approxGivensQuat(matrix3f.get(1, 1), 0.5f * (matrix3f.get(1, 2) + matrix3f.get(2, 1)), matrix3f.get(2, 2));
            float_ = pair.getFirst();
            float2 = pair.getSecond();
            quaternion2 = new Quaternion(float_.floatValue(), 0.0f, 0.0f, float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
            g = -2.0f * float_.floatValue() * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
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
        Quaternion quaternion = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
        Quaternion quaternion2 = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
        Matrix3f matrix3f = new Matrix3f(this, true);
        matrix3f.mul(this);
        for (int i = 0; i < 5; ++i) {
            quaternion2.mul(Matrix3f.stepJacobi(matrix3f));
        }
        quaternion2.normalize();
        Matrix3f matrix3f2 = new Matrix3f(this);
        matrix3f2.mul(new Matrix3f(quaternion2));
        float f = 1.0f;
        Pair<Float, Float> pair = Matrix3f.qrGivensQuat(matrix3f2.get(0, 0), matrix3f2.get(1, 0));
        Float float_ = pair.getFirst();
        Float float2 = pair.getSecond();
        float g = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
        float h = -2.0f * float_.floatValue() * float2.floatValue();
        float j = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
        Quaternion quaternion3 = new Quaternion(0.0f, 0.0f, float_.floatValue(), float2.floatValue());
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
        pair = Matrix3f.qrGivensQuat(matrix3f3.get(0, 0), matrix3f3.get(2, 0));
        float k = -pair.getFirst().floatValue();
        Float float3 = pair.getSecond();
        float l = float3.floatValue() * float3.floatValue() - k * k;
        float m = -2.0f * k * float3.floatValue();
        float n = float3.floatValue() * float3.floatValue() + k * k;
        Quaternion quaternion4 = new Quaternion(0.0f, k, 0.0f, float3.floatValue());
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
        pair = Matrix3f.qrGivensQuat(matrix3f4.get(1, 1), matrix3f4.get(2, 1));
        Float float4 = pair.getFirst();
        Float float5 = pair.getSecond();
        float o = float5.floatValue() * float5.floatValue() - float4.floatValue() * float4.floatValue();
        float p = -2.0f * float4.floatValue() * float5.floatValue();
        float q = float5.floatValue() * float5.floatValue() + float4.floatValue() * float4.floatValue();
        Quaternion quaternion5 = new Quaternion(float4.floatValue(), 0.0f, 0.0f, float5.floatValue());
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
        f = 1.0f / f;
        quaternion.mul((float)Math.sqrt(f));
        Vector3f vector3f = new Vector3f(matrix3f5.get(0, 0) * f, matrix3f5.get(1, 1) * f, matrix3f5.get(2, 2) * f);
        return Triple.of(quaternion, vector3f, quaternion2);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Matrix3f matrix3f = (Matrix3f)object;
        return Arrays.equals(this.values, matrix3f.values);
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
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                stringBuilder.append(this.values[i + j * 3]);
                if (j == 2) continue;
                stringBuilder.append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void setIdentity() {
        this.values[0] = 1.0f;
        this.values[1] = 0.0f;
        this.values[2] = 0.0f;
        this.values[3] = 0.0f;
        this.values[4] = 1.0f;
        this.values[5] = 0.0f;
        this.values[6] = 0.0f;
        this.values[7] = 0.0f;
        this.values[8] = 1.0f;
    }

    public float get(int i, int j) {
        return this.values[3 * j + i];
    }

    public void set(int i, int j, float f) {
        this.values[3 * j + i] = f;
    }

    public void mul(Matrix3f matrix3f) {
        float[] fs = Arrays.copyOf(this.values, 9);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.values[i + j * 3] = 0.0f;
                for (int k = 0; k < 3; ++k) {
                    int n = i + j * 3;
                    this.values[n] = this.values[n] + fs[i + k * 3] * matrix3f.values[k + j * 3];
                }
            }
        }
    }

    public void mul(Quaternion quaternion) {
        this.mul(new Matrix3f(quaternion));
    }

    public Matrix3f copy() {
        return new Matrix3f((float[])this.values.clone());
    }
}

