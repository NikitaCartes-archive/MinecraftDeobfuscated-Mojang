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
import net.minecraft.world.level.levelgen.placement.DecoratorFrequencyChance;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CountChanceHeightmapDecorator
extends FeatureDecorator<DecoratorFrequencyChance> {
    public CountChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorFrequencyChance> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DecoratorFrequencyChance decoratorFrequencyChance, BlockPos blockPos) {
        return IntStream.range(0, decoratorFrequencyChance.count).filter(i -> random.nextFloat() < decoratorFrequencyChance.chance).mapToObj(i -> {
            int j = random.nextInt(16);
            int k = random.nextInt(16);
            return levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k));
        });
    }
}

