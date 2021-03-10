/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class HeightmapDoubleDecorator
extends FeatureDecorator<HeightmapConfiguration> {
    public HeightmapDoubleDecorator(Codec<HeightmapConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, HeightmapConfiguration heightmapConfiguration, BlockPos blockPos) {
        int j;
        int i = blockPos.getX();
        int k = decorationContext.getHeight(heightmapConfiguration.heightmap, i, j = blockPos.getZ());
        if (k == decorationContext.getMinBuildHeight()) {
            return Stream.of(new BlockPos[0]);
        }
        return Stream.of(new BlockPos(i, decorationContext.getMinBuildHeight() + random.nextInt((k - decorationContext.getMinBuildHeight()) * 2), j));
    }
}

