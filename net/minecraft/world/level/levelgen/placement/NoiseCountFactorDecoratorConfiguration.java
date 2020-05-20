/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoiseCountFactorDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<NoiseCountFactorDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("noise_to_count_ratio")).forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseToCountRatio), ((MapCodec)Codec.DOUBLE.fieldOf("noise_factor")).forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseFactor), ((MapCodec)Codec.DOUBLE.fieldOf("noise_offset")).withDefault(0.0).forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseOffset), ((MapCodec)Heightmap.Types.CODEC.fieldOf("heightmap")).forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.heightmap)).apply((Applicative<NoiseCountFactorDecoratorConfiguration, ?>)instance, NoiseCountFactorDecoratorConfiguration::new));
    private static final Logger LOGGER = LogManager.getLogger();
    public final int noiseToCountRatio;
    public final double noiseFactor;
    public final double noiseOffset;
    public final Heightmap.Types heightmap;

    public NoiseCountFactorDecoratorConfiguration(int i, double d, double e, Heightmap.Types types) {
        this.noiseToCountRatio = i;
        this.noiseFactor = d;
        this.noiseOffset = e;
        this.heightmap = types;
    }
}

