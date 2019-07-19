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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class ChorusPlantPlacementDecorator
extends FeatureDecorator<NoneDecoratorConfiguration> {
    public ChorusPlantPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        int i2 = random.nextInt(5);
        return IntStream.range(0, i2).mapToObj(i -> {
            int k;
            int j = random.nextInt(16);
            int l = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k = random.nextInt(16))).getY();
            if (l > 0) {
                int m = l - 1;
                return new BlockPos(blockPos.getX() + j, m, blockPos.getZ() + k);
            }
            return null;
        }).filter(Objects::nonNull);
    }
}

