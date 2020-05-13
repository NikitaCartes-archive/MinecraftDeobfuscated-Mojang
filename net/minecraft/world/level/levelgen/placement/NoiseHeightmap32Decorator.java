/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class NoiseHeightmap32Decorator
extends FeatureDecorator<NoiseDependantDecoratorConfiguration> {
    public NoiseHeightmap32Decorator(Function<Dynamic<?>, ? extends NoiseDependantDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, NoiseDependantDecoratorConfiguration noiseDependantDecoratorConfiguration, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
        int i2 = d < noiseDependantDecoratorConfiguration.noiseLevel ? noiseDependantDecoratorConfiguration.belowNoise : noiseDependantDecoratorConfiguration.aboveNoise;
        return IntStream.range(0, i2).mapToObj(i -> {
            int k;
            int j = random.nextInt(16) + blockPos.getX();
            int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k = random.nextInt(16) + blockPos.getZ()) + 32;
            if (l <= 0) {
                return null;
            }
            return new BlockPos(j, random.nextInt(l), k);
        }).filter(Objects::nonNull);
    }
}

