/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

public final class Quaternion {
    public static final Quaternion ONE = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    private float i;
    private float j;
    private float k;
    private float r;

    public Quaternion(float f, float g, float h, float i) {
        this.i = f;
        this.j = g;
        this.k = h;
        this.r = i;
    }

    public Quaternion(Vector3f vector3f, float f, boolean bl) {
        if (bl) {
            f *= (float)Math.PI / 180;
        }
        float g = Quaternion.sin(f / 2.0f);
        this.i = vector3f.x() * g;
        this.j = vector3f.y() * g;
        this.k = vector3f.z() * g;
        this.r = Quaternion.cos(f / 2.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public Quaternion(float f, float g, float h, boolean bl) {
        if (bl) {
            f *= (float)Math.PI / 180;
            g *= (float)Math.PI / 180;
            h *= (float)Math.PI / 180;
        }
        float i = Quaternion.sin(0.5f * f);
        float j = Quaternion.cos(0.5f * f);
        float k = Quaternion.sin(0.5f * g);
        float l = Quaternion.cos(0.5f * g);
        float m = Quaternion.sin(0.5f * h);
        float n = Quaternion.cos(0.5f * h);
        this.i = i * l * n + j * k * m;
        this.j = j * k * n - i * l * m;
        this.k = i * k * n + j * l * m;
        this.r = j * l * n - i * k * m;
    }

    public Quaternion(Quaternion quaternion) {
        this.i = quaternion.i;
        this.j = quaternion.j;
        this.k = quaternion.k;
        this.r = quaternion.r;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Quaternion quaternion = (Quaternion)object;
        if (Float.compare(quaternion.i, this.i) != 0) {
            return false;
        }
        if (Float.compare(quaternion.j, this.j) != 0) {
            return false;
        }
        if (Float.compare(quaternion.k, this.k) != 0) {
            return false;
        }
        return Float.compare(quaternion.r, this.r) == 0;
    }

    public int hashCode() {
        int i = Float.floatToIntBits(this.i);
        i = 31 * i + Float.floatToIntBits(this.j);
        i = 31 * i + Float.floatToIntBits(this.k);
        i = 31 * i + Float.floatToIntBits(this.r);
        return i;
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
        return this.i;
    }

    public float j() {
        return this.j;
    }

    public float k() {
        return this.k;
    }

    public float r() {
        return this.r;
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
        this.i = i * j + f * m + g * l - h * k;
        this.j = i * k - f * l + g * m + h * j;
        this.k = i * l + f * k - g * j + h * m;
        this.r = i * m - f * j - g * k - h * l;
    }

    @Environment(value=EnvType.CLIENT)
    public void mul(float f) {
        this.i *= f;
        this.j *= f;
        this.k *= f;
        this.r *= f;
    }

    public void conj() {
        this.i = -this.i;
        this.j = -this.j;
        this.k = -this.k;
    }

    @Environment(value=EnvType.CLIENT)
    public void set(float f, float g, float h, float i) {
        this.i = f;
        this.j = g;
        this.k = h;
        this.r = i;
    }

    private static float cos(float f) {
        return (float)Math.cos(f);
    }

    private static float sin(float f) {
        return (float)Math.sin(f);
    }

    @Environment(value=EnvType.CLIENT)
    public void normalize() {
        float f = this.i() * this.i() + this.j() * this.j() + this.k() * this.k() + this.r() * this.r();
        if (f > 1.0E-6f) {
            float g = Mth.fastInvSqrt(f);
            this.i *= g;
            this.j *= g;
            this.k *= g;
            this.r *= g;
        } else {
            this.i = 0.0f;
            this.j = 0.0f;
            this.k = 0.0f;
            this.r = 0.0f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public Quaternion copy() {
        return new Quaternion(this);
    }
}

