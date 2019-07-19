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
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.MonsterRoomPlacementConfiguration;

public class MonsterRoomPlacementDecorator
extends FeatureDecorator<MonsterRoomPlacementConfiguration> {
    public MonsterRoomPlacementDecorator(Function<Dynamic<?>, ? extends MonsterRoomPlacementConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, MonsterRoomPlacementConfiguration monsterRoomPlacementConfiguration, BlockPos blockPos) {
        int i2 = monsterRoomPlacementConfiguration.chance;
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16);
            int k = random.nextInt(chunkGenerator.getGenDepth());
            int l = random.nextInt(16);
            return blockPos.offset(j, k, l);
        });
    }
}

