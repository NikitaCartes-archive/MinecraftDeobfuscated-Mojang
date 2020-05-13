/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class ChanceHeightmapDoubleDecorator
extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public ChanceHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / (float)chanceDecoratorConfiguration.chance) {
            int j;
            int i = random.nextInt(16) + blockPos.getX();
            int k = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j = random.nextInt(16) + blockPos.getZ()) * 2;
            if (k <= 0) {
                return Stream.empty();
            }
            return Stream.of(new BlockPos(i, random.nextInt(k), j));
        }
        return Stream.empty();
    }
}

