/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.BlockFilterConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FilterDecorator;

public class BlockFilterDecorator
extends FilterDecorator<BlockFilterConfiguration> {
    public BlockFilterDecorator(Codec<BlockFilterConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean shouldPlace(DecorationContext decorationContext, Random random, BlockFilterConfiguration blockFilterConfiguration, BlockPos blockPos) {
        return blockFilterConfiguration.predicate().test(decorationContext.getLevel(), blockPos);
    }
}

