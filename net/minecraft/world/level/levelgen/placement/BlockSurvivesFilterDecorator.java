/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.SingleBlockStateConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class BlockSurvivesFilterDecorator
extends FeatureDecorator<SingleBlockStateConfiguration> {
    public BlockSurvivesFilterDecorator(Codec<SingleBlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, SingleBlockStateConfiguration singleBlockStateConfiguration, BlockPos blockPos) {
        if (!singleBlockStateConfiguration.state().canSurvive(decorationContext.getLevel(), blockPos)) {
            return Stream.of(new BlockPos[0]);
        }
        return Stream.of(blockPos);
    }
}

