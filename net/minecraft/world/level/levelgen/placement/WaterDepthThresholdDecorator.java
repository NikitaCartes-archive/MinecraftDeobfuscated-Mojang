/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.WaterDepthThresholdConfiguration;

public class WaterDepthThresholdDecorator
extends FeatureDecorator<WaterDepthThresholdConfiguration> {
    public WaterDepthThresholdDecorator(Codec<WaterDepthThresholdConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, WaterDepthThresholdConfiguration waterDepthThresholdConfiguration, BlockPos blockPos) {
        int i = decorationContext.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ());
        int j = decorationContext.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ());
        if (j - i > waterDepthThresholdConfiguration.maxWaterDepth) {
            return Stream.of(new BlockPos[0]);
        }
        return Stream.of(blockPos);
    }
}

