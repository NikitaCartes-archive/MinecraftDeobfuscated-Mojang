/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class AmbientParticleSettings {
    public static final Codec<AmbientParticleSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ParticleTypes.CODEC.fieldOf("options")).forGetter(ambientParticleSettings -> ambientParticleSettings.options), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(ambientParticleSettings -> Float.valueOf(ambientParticleSettings.probability))).apply((Applicative<AmbientParticleSettings, ?>)instance, AmbientParticleSettings::new));
    private final ParticleOptions options;
    private final float probability;

    public AmbientParticleSettings(ParticleOptions particleOptions, float f) {
        this.options = particleOptions;
        this.probability = f;
    }

    @Environment(value=EnvType.CLIENT)
    public ParticleOptions getOptions() {
        return this.options;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean canSpawn(Random random) {
        return random.nextFloat() <= this.probability;
    }
}

