/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.NoiseCountFactorDecoratorConfiguration;

public class TopSolidHeightMapNoiseBasedDecorator
extends FeatureDecorator<NoiseCountFactorDecoratorConfiguration> {
    public TopSolidHeightMapNoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, NoiseCountFactorDecoratorConfiguration noiseCountFactorDecoratorConfiguration, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / noiseCountFactorDecoratorConfiguration.noiseFactor, (double)blockPos.getZ() / noiseCountFactorDecoratorConfiguration.noiseFactor, false);
        int i2 = (int)Math.ceil((d + noiseCountFactorDecoratorConfiguration.noiseOffset) * (double)noiseCountFactorDecoratorConfiguration.noiseToCountRatio);
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = levelAccessor.getHeight(noiseCountFactorDecoratorConfiguration.heightmap, j, k);
            return new BlockPos(j, l, k);
        });
    }
}

