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
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class EndGatewayPlacementDecorator
extends FeatureDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        int j;
        int i;
        int k;
        if (random.nextInt(700) == 0 && (k = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(i = random.nextInt(16), 0, j = random.nextInt(16))).getY()) > 0) {
            int l = k + 3 + random.nextInt(7);
            return Stream.of(blockPos.offset(i, l, j));
        }
        return Stream.empty();
    }
}

