/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityDimensions {
    public final float width;
    public final float height;
    public final boolean fixed;

    public EntityDimensions(float f, float g, boolean bl) {
        this.width = f;
        this.height = g;
        this.fixed = bl;
    }

    public AABB makeBoundingBox(Vec3 vec3) {
        return this.makeBoundingBox(vec3.x, vec3.y, vec3.z);
    }

    public AABB makeBoundingBox(double d, double e, double f) {
        float g = this.width / 2.0f;
        float h = this.height;
        return new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)h, f + (double)g);
    }

    public EntityDimensions scale(float f) {
        return this.scale(f, f);
    }

    public EntityDimensions scale(float f, float g) {
        if (this.fixed || f == 1.0f && g == 1.0f) {
            return this;
        }
        return EntityDimensions.scalable(this.width * f, this.height * g);
    }

    public static EntityDimensions scalable(float f, float g) {
        return new EntityDimensions(f, g, false);
    }

    public static EntityDimensions fixed(float f, float g) {
        return new EntityDimensions(f, g, true);
    }

    public String toString() {
        return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
    }
}

