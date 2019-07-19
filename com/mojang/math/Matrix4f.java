/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.math.Quaternion;
import java.nio.FloatBuffer;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class Matrix4f {
    private final float[] values = new float[16];

    public Matrix4f() {
    }

    public Matrix4f(Quaternion quaternion) {
        this();
        float f = quaternion.i();
        float g = quaternion.j();
        float h = quaternion.k();
        float i = quaternion.r();
        float j = 2.0f * f * f;
        float k = 2.0f * g * g;
        float l = 2.0f * h * h;
        this.values[0] = 1.0f - k - l;
        this.values[5] = 1.0f - l - j;
        this.values[10] = 1.0f - j - k;
        this.values[15] = 1.0f;
        float m = f * g;
        float n = g * h;
        float o = h * f;
        float p = f * i;
        float q = g * i;
        float r = h * i;
        this.values[1] = 2.0f * (m + r);
        this.values[4] = 2.0f * (m - r);
        this.values[2] = 2.0f * (o - q);
        this.values[8] = 2.0f * (o + q);
        this.values[6] = 2.0f * (n + p);
        this.values[9] = 2.0f * (n - p);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Matrix4f matrix4f = (Matrix4f)object;
        return Arrays.equals(this.values, matrix4f.values);
    }

    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public void load(FloatBuffer floatBuffer) {
        this.load(floatBuffer, false);
    }

    public void load(FloatBuffer floatBuffer, boolean bl) {
        if (bl) {
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    this.values[i * 4 + j] = floatBuffer.get(j * 4 + i);
                }
            }
        } else {
            floatBuffer.get(this.values);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Matrix4f:\n");
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                stringBuilder.append(this.values[i + j * 4]);
                if (j == 3) continue;
                stringBuilder.append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void store(FloatBuffer floatBuffer) {
        this.store(floatBuffer, false);
    }

    public void store(FloatBuffer floatBuffer, boolean bl) {
        if (bl) {
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    floatBuffer.put(j * 4 + i, this.values[i * 4 + j]);
                }
            }
        } else {
            floatBuffer.put(this.values);
        }
    }

    public void set(int i, int j, float f) {
        this.values[i + 4 * j] = f;
    }

    public static Matrix4f perspective(double d, float f, float g, float h) {
        float i = (float)(1.0 / Math.tan(d * 0.01745329238474369 / 2.0));
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.set(0, 0, i / f);
        matrix4f.set(1, 1, i);
        matrix4f.set(2, 2, (h + g) / (g - h));
        matrix4f.set(3, 2, -1.0f);
        matrix4f.set(2, 3, 2.0f * h * g / (g - h));
        return matrix4f;
    }

    public static Matrix4f orthographic(float f, float g, float h, float i) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.set(0, 0, 2.0f / f);
        matrix4f.set(1, 1, 2.0f / g);
        float j = i - h;
        matrix4f.set(2, 2, -2.0f / j);
        matrix4f.set(3, 3, 1.0f);
        matrix4f.set(0, 3, -1.0f);
        matrix4f.set(1, 3, -1.0f);
        matrix4f.set(2, 3, -(i + h) / j);
        return matrix4f;
    }
}

