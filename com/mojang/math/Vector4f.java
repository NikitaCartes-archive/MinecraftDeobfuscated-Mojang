/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class Vector4f {
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4f() {
    }

    public Vector4f(float f, float g, float h, float i) {
        this.x = f;
        this.y = g;
        this.z = h;
        this.w = i;
    }

    public Vector4f(Vector3f vector3f) {
        this(vector3f.x(), vector3f.y(), vector3f.z(), 1.0f);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Vector4f vector4f = (Vector4f)object;
        if (Float.compare(vector4f.x, this.x) != 0) {
            return false;
        }
        if (Float.compare(vector4f.y, this.y) != 0) {
            return false;
        }
        if (Float.compare(vector4f.z, this.z) != 0) {
            return false;
        }
        return Float.compare(vector4f.w, this.w) == 0;
    }

    public int hashCode() {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        i = 31 * i + Float.floatToIntBits(this.z);
        i = 31 * i + Float.floatToIntBits(this.w);
        return i;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public void mul(Vector3f vector3f) {
        this.x *= vector3f.x();
        this.y *= vector3f.y();
        this.z *= vector3f.z();
    }

    public float dot(Vector4f vector4f) {
        return this.x * vector4f.x + this.y * vector4f.y + this.z * vector4f.z + this.w * vector4f.w;
    }

    public boolean normalize() {
        float f = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
        if ((double)f < 1.0E-5) {
            return false;
        }
        float g = Mth.fastInvSqrt(f);
        this.x *= g;
        this.y *= g;
        this.z *= g;
        this.w *= g;
        return true;
    }

    public void transform(Matrix4f matrix4f) {
        float f = this.x;
        float g = this.y;
        float h = this.z;
        float i = this.w;
        this.x = Vector4f.multiplyRow(0, matrix4f, f, g, h, i);
        this.y = Vector4f.multiplyRow(1, matrix4f, f, g, h, i);
        this.z = Vector4f.multiplyRow(2, matrix4f, f, g, h, i);
        this.w = Vector4f.multiplyRow(3, matrix4f, f, g, h, i);
    }

    private static float multiplyRow(int i, Matrix4f matrix4f, float f, float g, float h, float j) {
        return matrix4f.get(i, 0) * f + matrix4f.get(i, 1) * g + matrix4f.get(i, 2) * h + matrix4f.get(i, 3) * j;
    }

    public void perspectiveDivide() {
        this.x /= this.w;
        this.y /= this.w;
        this.z /= this.w;
        this.w = 1.0f;
    }
}

