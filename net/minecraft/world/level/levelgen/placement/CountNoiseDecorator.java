/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class CountNoiseDecorator
extends RepeatingDecorator<NoiseDependantDecoratorConfiguration> {
    public CountNoiseDecorator(Codec<NoiseDependantDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int count(Random random, NoiseDependantDecoratorConfiguration noiseDependantDecoratorConfiguration, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
        return d < noiseDependantDecoratorConfiguration.noiseLevel ? noiseDependantDecoratorConfiguration.belowNoise : noiseDependantDecoratorConfiguration.aboveNoise;
    }
}

