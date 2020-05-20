/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;

public class CountHeightmap32Decorator
extends FeatureDecorator<FrequencyDecoratorConfiguration> {
    public CountHeightmap32Decorator(Codec<FrequencyDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos) {
        return IntStream.range(0, frequencyDecoratorConfiguration.count).mapToObj(i -> {
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

