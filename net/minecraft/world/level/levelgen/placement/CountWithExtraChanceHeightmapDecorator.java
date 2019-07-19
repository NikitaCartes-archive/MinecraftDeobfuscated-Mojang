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
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequencyWithExtraChance;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CountWithExtraChanceHeightmapDecorator
extends FeatureDecorator<DecoratorFrequencyWithExtraChance> {
    public CountWithExtraChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorFrequencyWithExtraChance> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DecoratorFrequencyWithExtraChance decoratorFrequencyWithExtraChance, BlockPos blockPos) {
        int i2 = decoratorFrequencyWithExtraChance.count;
        if (random.nextFloat() < decoratorFrequencyWithExtraChance.extraChance) {
            i2 += decoratorFrequencyWithExtraChance.extraCount;
        }
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16);
            int k = random.nextInt(16);
            return levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k));
        });
    }
}

