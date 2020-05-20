/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoiseDependantDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<NoiseDependantDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("noise_level")).forGetter(noiseDependantDecoratorConfiguration -> noiseDependantDecoratorConfiguration.noiseLevel), ((MapCodec)Codec.INT.fieldOf("below_noise")).forGetter(noiseDependantDecoratorConfiguration -> noiseDependantDecoratorConfiguration.belowNoise), ((MapCodec)Codec.INT.fieldOf("above_noise")).forGetter(noiseDependantDecoratorConfiguration -> noiseDependantDecoratorConfiguration.aboveNoise)).apply((Applicative<NoiseDependantDecoratorConfiguration, ?>)instance, NoiseDependantDecoratorConfiguration::new));
    public final double noiseLevel;
    public final int belowNoise;
    public final int aboveNoise;

    public NoiseDependantDecoratorConfiguration(double d, int i, int j) {
        this.noiseLevel = d;
        this.belowNoise = i;
        this.aboveNoise = j;
    }
}

