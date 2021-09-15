/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdConfiguration;

public class SurfaceRelativeThresholdDecorator
extends FeatureDecorator<SurfaceRelativeThresholdConfiguration> {
    public SurfaceRelativeThresholdDecorator(Codec<SurfaceRelativeThresholdConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, SurfaceRelativeThresholdConfiguration surfaceRelativeThresholdConfiguration, BlockPos blockPos) {
        long l = decorationContext.getHeight(surfaceRelativeThresholdConfiguration.heightmap, blockPos.getX(), blockPos.getZ());
        long m = l + (long)surfaceRelativeThresholdConfiguration.minInclusive;
        long n = l + (long)surfaceRelativeThresholdConfiguration.maxInclusive;
        if ((long)blockPos.getY() < m || (long)blockPos.getY() > n) {
            return Stream.of(new BlockPos[0]);
        }
        return Stream.of(blockPos);
    }
}

