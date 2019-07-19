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
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.DecoratorNoiseDependant;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class NoiseHeightmap32Decorator
extends FeatureDecorator<DecoratorNoiseDependant> {
    public NoiseHeightmap32Decorator(Function<Dynamic<?>, ? extends DecoratorNoiseDependant> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DecoratorNoiseDependant decoratorNoiseDependant, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0);
        int i2 = d < decoratorNoiseDependant.noiseLevel ? decoratorNoiseDependant.belowNoise : decoratorNoiseDependant.aboveNoise;
        return IntStream.range(0, i2).mapToObj(i -> {
            int k;
            int j = random.nextInt(16);
            int l = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k = random.nextInt(16))).getY() + 32;
            if (l <= 0) {
                return null;
            }
            int m = random.nextInt(l);
            return blockPos.offset(j, m, k);
        }).filter(Objects::nonNull);
    }
}

