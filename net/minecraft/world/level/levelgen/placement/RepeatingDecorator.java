/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public abstract class RepeatingDecorator<DC extends DecoratorConfiguration>
extends FeatureDecorator<DC> {
    public RepeatingDecorator(Codec<DC> codec) {
        super(codec);
    }

    protected abstract int count(Random var1, DC var2, BlockPos var3);

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
        return IntStream.range(0, this.count(random, decoratorConfiguration, blockPos)).mapToObj(i -> blockPos);
    }
}

