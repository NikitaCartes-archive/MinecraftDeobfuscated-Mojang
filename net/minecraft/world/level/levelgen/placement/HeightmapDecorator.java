/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class HeightmapDecorator
extends FeatureDecorator<HeightmapConfiguration> {
    public HeightmapDecorator(Codec<HeightmapConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, HeightmapConfiguration heightmapConfiguration, BlockPos blockPos) {
        int j;
        int i = blockPos.getX();
        int k = decorationContext.getHeight(heightmapConfiguration.heightmap, i, j = blockPos.getZ());
        if (k > decorationContext.getMinBuildHeight()) {
            return Stream.of(new BlockPos(i, k, j));
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public /* synthetic */ Stream getPositions(DecorationContext decorationContext, Random random, DecoratorConfiguration decoratorConfiguration, BlockPos blockPos) {
        return this.getPositions(decorationContext, random, (HeightmapConfiguration)decoratorConfiguration, blockPos);
    }
}

