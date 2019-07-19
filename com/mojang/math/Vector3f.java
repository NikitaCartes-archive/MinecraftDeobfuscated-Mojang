/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import com.mojang.math.Quaternion;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

public final class Vector3f {
    private final float[] values;

    @Environment(value=EnvType.CLIENT)
    public Vector3f(Vector3f vector3f) {
        this.values = Arrays.copyOf(vector3f.values, 3);
    }

    public Vector3f() {
        this.values = new float[3];
    }

    @Environment(value=EnvType.CLIENT)
    public Vector3f(float f, float g, float h) {
        this.values = new float[]{f, g, h};
    }

    public Vector3f(Vec3 vec3) {
        this.values = new float[]{(float)vec3.x, (float)vec3.y, (float)vec3.z};
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Vector3f vector3f = (Vector3f)object;
        return Arrays.equals(this.values, vector3f.values);
    }

    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public float x() {
        return this.values[0];
    }

    public float y() {
        return this.values[1];
    }

    public float z() {
        return this.values[2];
    }

    @Environment(value=EnvType.CLIENT)
    public void mul(float f) {
        int i = 0;
        while (i < 3) {
            int n = i++;
            this.values[n] = this.values[n] * f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static float clamp(float f, float g, float h) {
        if (f < g) {
            return g;
        }
        if (f > h) {
            return h;
        }
        return f;
    }

    @Environment(value=EnvType.CLIENT)
    public void clamp(float f, float g) {
        this.values[0] = Vector3f.clamp(this.values[0], f, g);
        this.values[1] = Vector3f.clamp(this.values[1], f, g);
        this.values[2] = Vector3f.clamp(this.values[2], f, g);
    }

    public void set(float f, float g, float h) {
        this.values[0] = f;
        this.values[1] = g;
        this.values[2] = h;
    }

    @Environment(value=EnvType.CLIENT)
    public void add(float f, float g, float h) {
        this.values[0] = this.values[0] + f;
        this.values[1] = this.values[1] + g;
        this.values[2] = this.values[2] + h;
    }

    @Environment(value=EnvType.CLIENT)
    public void sub(Vector3f vector3f) {
        for (int i = 0; i < 3; ++i) {
            int n = i;
            this.values[n] = this.values[n] - vector3f.values[i];
        }
    }

    @Environment(value=EnvType.CLIENT)
    public float dot(Vector3f vector3f) {
        float f = 0.0f;
        for (int i = 0; i < 3; ++i) {
            f += this.values[i] * vector3f.values[i];
        }
        return f;
    }

    @Environment(value=EnvType.CLIENT)
    public void normalize() {
        int i;
        float f = 0.0f;
        for (i = 0; i < 3; ++i) {
            f += this.values[i] * this.values[i];
        }
        i = 0;
        while (i < 3) {
            int n = i++;
            this.values[n] = this.values[n] / f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public void cross(Vector3f vector3f) {
        float f = this.values[0];
        float g = this.values[1];
        float h = this.values[2];
        float i = vector3f.x();
        float j = vector3f.y();
        float k = vector3f.z();
        this.values[0] = g * k - h * j;
        this.values[1] = h * i - f * k;
        this.values[2] = f * j - g * i;
    }

    public void transform(Quaternion quaternion) {
        Quaternion quaternion2 = new Quaternion(quaternion);
        quaternion2.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0f));
        Quaternion quaternion3 = new Quaternion(quaternion);
        quaternion3.conj();
        quaternion2.mul(quaternion3);
        this.set(quaternion2.i(), quaternion2.j(), quaternion2.k());
    }
}

