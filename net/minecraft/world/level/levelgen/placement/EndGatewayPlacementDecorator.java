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
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class EndGatewayPlacementDecorator
extends FeatureDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        int j;
        int i;
        int k;
        if (random.nextInt(700) == 0 && (k = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, i = random.nextInt(16) + blockPos.getX(), j = random.nextInt(16) + blockPos.getZ())) > 0) {
            int l = k + 3 + random.nextInt(7);
            return Stream.of(new BlockPos(i, l, j));
        }
        return Stream.empty();
    }
}

