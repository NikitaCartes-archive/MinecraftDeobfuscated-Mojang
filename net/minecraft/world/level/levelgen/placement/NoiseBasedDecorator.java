/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.NoiseCountFactorDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class NoiseBasedDecorator
extends RepeatingDecorator<NoiseCountFactorDecoratorConfiguration> {
    public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int count(Random random, NoiseCountFactorDecoratorConfiguration noiseCountFactorDecoratorConfiguration, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / noiseCountFactorDecoratorConfiguration.noiseFactor, (double)blockPos.getZ() / noiseCountFactorDecoratorConfiguration.noiseFactor, false);
        return (int)Math.ceil((d + noiseCountFactorDecoratorConfiguration.noiseOffset) * (double)noiseCountFactorDecoratorConfiguration.noiseToCountRatio);
    }
}

