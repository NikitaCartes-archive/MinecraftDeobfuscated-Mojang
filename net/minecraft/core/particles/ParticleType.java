/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;

public abstract class ParticleType<T extends ParticleOptions> {
    private final boolean overrideLimiter;
    private final ParticleOptions.Deserializer<T> deserializer;

    protected ParticleType(boolean bl, ParticleOptions.Deserializer<T> deserializer) {
        this.overrideLimiter = bl;
        this.deserializer = deserializer;
    }

    public boolean getOverrideLimiter() {
        return this.overrideLimiter;
    }

    public ParticleOptions.Deserializer<T> getDeserializer() {
        return this.deserializer;
    }

    public abstract Codec<T> codec();
}

