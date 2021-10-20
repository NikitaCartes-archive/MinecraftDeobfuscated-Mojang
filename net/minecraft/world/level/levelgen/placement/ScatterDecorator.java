/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ScatterDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class ScatterDecorator
extends FeatureDecorator<ScatterDecoratorConfiguration> {
    public ScatterDecorator(Codec<ScatterDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, ScatterDecoratorConfiguration scatterDecoratorConfiguration, BlockPos blockPos) {
        int i = blockPos.getX() + scatterDecoratorConfiguration.xzSpread.sample(random);
        int j = blockPos.getY() + scatterDecoratorConfiguration.ySpread.sample(random);
        int k = blockPos.getZ() + scatterDecoratorConfiguration.xzSpread.sample(random);
        BlockPos blockPos2 = new BlockPos(i, j, k);
        ChunkPos chunkPos = new ChunkPos(blockPos2);
        ChunkPos chunkPos2 = new ChunkPos(blockPos);
        int l = Mth.abs(chunkPos.x - chunkPos2.x);
        int m = Mth.abs(chunkPos.z - chunkPos2.z);
        if (l > 1 || m > 1) {
            return Stream.empty();
        }
        return Stream.of(new BlockPos(i, j, k));
    }

    @Override
    public /* synthetic */ Stream getPositions(DecorationContext decorationContext, Random random, DecoratorConfiguration decoratorConfiguration, BlockPos blockPos) {
        return this.getPositions(decorationContext, random, (ScatterDecoratorConfiguration)decoratorConfiguration, blockPos);
    }
}

