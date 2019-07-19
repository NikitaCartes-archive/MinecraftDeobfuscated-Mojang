/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys;

import net.minecraft.world.phys.Vec3;

public abstract class HitResult {
    protected final Vec3 location;

    protected HitResult(Vec3 vec3) {
        this.location = vec3;
    }

    public abstract Type getType();

    public Vec3 getLocation() {
        return this.location;
    }

    public static enum Type {
        MISS,
        BLOCK,
        ENTITY;

    }
}

