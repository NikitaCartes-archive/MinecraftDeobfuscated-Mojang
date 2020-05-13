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

public class ChanceHeightmapDecorator
extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public ChanceHeightmapDecorator(Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / (float)chanceDecoratorConfiguration.chance) {
            int i = random.nextInt(16) + blockPos.getX();
            int j = random.nextInt(16) + blockPos.getZ();
            int k = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j);
            return Stream.of(new BlockPos(i, k, j));
        }
        return Stream.empty();
    }
}

