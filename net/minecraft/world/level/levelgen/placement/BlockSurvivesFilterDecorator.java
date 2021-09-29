/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.SingleBlockStateConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FilterDecorator;

public class BlockSurvivesFilterDecorator
extends FilterDecorator<SingleBlockStateConfiguration> {
    public BlockSurvivesFilterDecorator(Codec<SingleBlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean shouldPlace(DecorationContext decorationContext, Random random, SingleBlockStateConfiguration singleBlockStateConfiguration, BlockPos blockPos) {
        return singleBlockStateConfiguration.state().canSurvive(decorationContext.getLevel(), blockPos);
    }
}

