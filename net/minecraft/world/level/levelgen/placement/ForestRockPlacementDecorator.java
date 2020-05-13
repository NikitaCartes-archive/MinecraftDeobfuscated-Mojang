/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;

public class ForestRockPlacementDecorator
extends FeatureDecorator<FrequencyDecoratorConfiguration> {
    public ForestRockPlacementDecorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos) {
        int i2 = random.nextInt(frequencyDecoratorConfiguration.count);
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k);
            return new BlockPos(j, l, k);
        });
    }
}

