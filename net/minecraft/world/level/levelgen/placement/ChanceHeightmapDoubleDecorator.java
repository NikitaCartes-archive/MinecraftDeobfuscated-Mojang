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
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.DecoratorChance;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class ChanceHeightmapDoubleDecorator
extends FeatureDecorator<DecoratorChance> {
    public ChanceHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends DecoratorChance> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DecoratorChance decoratorChance, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / (float)decoratorChance.chance) {
            int j;
            int i = random.nextInt(16);
            int k = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(i, 0, j = random.nextInt(16))).getY() * 2;
            if (k <= 0) {
                return Stream.empty();
            }
            int l = random.nextInt(k);
            return Stream.of(blockPos.offset(i, l, j));
        }
        return Stream.empty();
    }
}

