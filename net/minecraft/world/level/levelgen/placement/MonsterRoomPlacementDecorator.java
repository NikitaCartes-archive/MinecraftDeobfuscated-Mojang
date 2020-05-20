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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class MonsterRoomPlacementDecorator
extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public MonsterRoomPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        int i2 = chanceDecoratorConfiguration.chance;
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = random.nextInt(chunkGenerator.getGenDepth());
            return new BlockPos(j, l, k);
        });
    }
}

