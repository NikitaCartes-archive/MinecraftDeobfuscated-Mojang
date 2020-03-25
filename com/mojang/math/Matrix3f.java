/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;

public final class Matrix3f {
    private static final float G = 3.0f + 2.0f * (float)Math.sqrt(2.0);
    private static final float CS = (float)Math.cos(0.39269908169872414);
    private static final float SS = (float)Math.sin(0.39269908169872414);
    private static final float SQ2 = 1.0f / (float)Math.sqrt(2.0);
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
        float j = 2.0f * f * f;
        float k = 2.0f * g * g;
        float l = 2.0f * h * h;
        this.m00 = 1.0f - k - l;
        this.m11 = 1.0f - l - j;
        this.m22 = 1.0f - j - k;
        float m = f * g;
        float n = g * h;
        float o = h * f;
        float p = f * i;
        float q = g * i;
        float r = h * i;
        this.m10 = 2.0f * (m + r);
        this.m01 = 2.0f * (m - r);
        this.m20 = 2.0f * (o - q);
        this.m02 = 2.0f * (o + q);
        this.m21 = 2.0f * (n + p);
        this.m12 = 2.0f * (n - p);
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
    private static Pair<Float, Float> approxGivensQuat(float f, float g, float h) {
        float j = g;
        float i = 2.0f * (f - h);
        if (G * j * j < i * i) {
            float k = Mth.fastInvSqrt(j * j + i * i);
            return Pair.of(Float.valueOf(k * j), Float.valueOf(k * i));
        }
        return Pair.of(Float.valueOf(SS), Float.valueOf(CS));
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
    private static Quaternion stepJacobi(Matrix3f matrix3f) {
        float h;
        float g;
        float f;
        Quaternion quaternion2;
        Float float2;
        Float float_;
        Pair<Float, Float> pair;
        Matrix3f matrix3f2 = new Matrix3f();
        Quaternion quaternion = Quaternion.ONE.copy();
        if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6f) {
            pair = Matrix3f.approxGivensQuat(matrix3f.m00, 0.5f * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
            float_ = pair.getFirst();
            float2 = pair.getSecond();
            quaternion2 = new Quaternion(0.0f, 0.0f, float_.floatValue(), float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
            g = -2.0f * float_.floatValue() * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
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
        if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6f) {
            pair = Matrix3f.approxGivensQuat(matrix3f.m00, 0.5f * (matrix3f.m02 + matrix3f.m20), matrix3f.m22);
            float i = -pair.getFirst().floatValue();
            float2 = pair.getSecond();
            quaternion2 = new Quaternion(0.0f, i, 0.0f, float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - i * i;
            g = -2.0f * i * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + i * i;
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
        if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6f) {
            pair = Matrix3f.approxGivensQuat(matrix3f.m11, 0.5f * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
            float_ = pair.getFirst();
            float2 = pair.getSecond();
            quaternion2 = new Quaternion(float_.floatValue(), 0.0f, 0.0f, float2.floatValue());
            f = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
            g = -2.0f * float_.floatValue() * float2.floatValue();
            h = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
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

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
    public Triple<Quaternion, Vector3f, Quaternion> svdDecompose() {
        Quaternion quaternion = Quaternion.ONE.copy();
        Quaternion quaternion2 = Quaternion.ONE.copy();
        Matrix3f matrix3f = this.copy();
        matrix3f.transpose();
        matrix3f.mul(this);
        for (int i = 0; i < 5; ++i) {
            quaternion2.mul(Matrix3f.stepJacobi(matrix3f));
        }
        quaternion2.normalize();
        Matrix3f matrix3f2 = new Matrix3f(this);
        matrix3f2.mul(new Matrix3f(quaternion2));
        float f = 1.0f;
        Pair<Float, Float> pair = Matrix3f.qrGivensQuat(matrix3f2.m00, matrix3f2.m10);
        Float float_ = pair.getFirst();
        Float float2 = pair.getSecond();
        float g = float2.floatValue() * float2.floatValue() - float_.floatValue() * float_.floatValue();
        float h = -2.0f * float_.floatValue() * float2.floatValue();
        float j = float2.floatValue() * float2.floatValue() + float_.floatValue() * float_.floatValue();
        Quaternion quaternion3 = new Quaternion(0.0f, 0.0f, float_.floatValue(), float2.floatValue());
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
        pair = Matrix3f.qrGivensQuat(matrix3f3.m00, matrix3f3.m20);
        float k = -pair.getFirst().floatValue();
        Float float3 = pair.getSecond();
        float l = float3.floatValue() * float3.floatValue() - k * k;
        float m = -2.0f * k * float3.floatValue();
        float n = float3.floatValue() * float3.floatValue() + k * k;
        Quaternion quaternion4 = new Quaternion(0.0f, k, 0.0f, float3.floatValue());
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
        pair = Matrix3f.qrGivensQuat(matrix3f4.m11, matrix3f4.m21);
        Float float4 = pair.getFirst();
        Float float5 = pair.getSecond();
        float o = float5.floatValue() * float5.floatValue() - float4.floatValue() * float4.floatValue();
        float p = -2.0f * float4.floatValue() * float5.floatValue();
        float q = float5.floatValue() * float5.floatValue() + float4.floatValue() * float4.floatValue();
        Quaternion quaternion5 = new Quaternion(float4.floatValue(), 0.0f, 0.0f, float5.floatValue());
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
        f = 1.0f / f;
        quaternion.mul((float)Math.sqrt(f));
        Vector3f vector3f = new Vector3f(matrix3f5.m00 * f, matrix3f5.m11 * f, matrix3f5.m22 * f);
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
        return Float.compare(matrix3f.m00, this.m00) == 0 && Float.compare(matrix3f.m01, this.m01) == 0 && Float.compare(matrix3f.m02, this.m02) == 0 && Float.compare(matrix3f.m10, this.m10) == 0 && Float.compare(matrix3f.m11, this.m11) == 0 && Float.compare(matrix3f.m12, this.m12) == 0 && Float.compare(matrix3f.m20, this.m20) == 0 && Float.compare(matrix3f.m21, this.m21) == 0 && Float.compare(matrix3f.m22, this.m22) == 0;
    }

    public int hashCode() {
        int i = this.m00 != 0.0f ? Float.floatToIntBits(this.m00) : 0;
        i = 31 * i + (this.m01 != 0.0f ? Float.floatToIntBits(this.m01) : 0);
        i = 31 * i + (this.m02 != 0.0f ? Float.floatToIntBits(this.m02) : 0);
        i = 31 * i + (this.m10 != 0.0f ? Float.floatToIntBits(this.m10) : 0);
        i = 31 * i + (this.m11 != 0.0f ? Float.floatToIntBits(this.m11) : 0);
        i = 31 * i + (this.m12 != 0.0f ? Float.floatToIntBits(this.m12) : 0);
        i = 31 * i + (this.m20 != 0.0f ? Float.floatToIntBits(this.m20) : 0);
        i = 31 * i + (this.m21 != 0.0f ? Float.floatToIntBits(this.m21) : 0);
        i = 31 * i + (this.m22 != 0.0f ? Float.floatToIntBits(this.m22) : 0);
        return i;
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
    public void setIdentity() {
        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
    public boolean invert() {
        float f = this.adjugateAndDet();
        if (Math.abs(f) > 1.0E-6f) {
            this.mul(f);
            return true;
        }
        return false;
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

    @Environment(value=EnvType.CLIENT)
    public void mul(Quaternion quaternion) {
        this.mul(new Matrix3f(quaternion));
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
    public Matrix3f copy() {
        return new Matrix3f(this);
    }
}

