/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.BlockFilterConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class BlockFilterDecorator
extends FeatureDecorator<BlockFilterConfiguration> {
    public BlockFilterDecorator(Codec<BlockFilterConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, BlockFilterConfiguration blockFilterConfiguration, BlockPos blockPos) {
        BlockState blockState = decorationContext.getLevel().getBlockState(blockPos.offset(blockFilterConfiguration.offset()));
        for (Block block : blockFilterConfiguration.disallowed()) {
            if (!blockState.is(block)) continue;
            return Stream.of(new BlockPos[0]);
        }
        for (Block block : blockFilterConfiguration.allowed()) {
            if (!blockState.is(block)) continue;
            return Stream.of(blockPos);
        }
        if (blockFilterConfiguration.allowed().isEmpty()) {
            return Stream.of(blockPos);
        }
        return Stream.of(new BlockPos[0]);
    }
}

