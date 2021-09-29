/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public abstract class FilterDecorator<DC extends DecoratorConfiguration>
extends FeatureDecorator<DC> {
    public FilterDecorator(Codec<DC> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
        if (this.shouldPlace(decorationContext, random, decoratorConfiguration, blockPos)) {
            return Stream.of(blockPos);
        }
        return Stream.of(new BlockPos[0]);
    }

    protected abstract boolean shouldPlace(DecorationContext var1, Random var2, DC var3, BlockPos var4);
}

