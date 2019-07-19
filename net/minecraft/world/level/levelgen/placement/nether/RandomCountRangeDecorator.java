/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class RandomCountRangeDecorator
extends SimpleFeatureDecorator<DecoratorCountRange> {
    public RandomCountRangeDecorator(Function<Dynamic<?>, ? extends DecoratorCountRange> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, DecoratorCountRange decoratorCountRange, BlockPos blockPos) {
        int i2 = random.nextInt(Math.max(decoratorCountRange.count, 1));
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16);
            int k = random.nextInt(decoratorCountRange.maximum - decoratorCountRange.topOffset) + decoratorCountRange.bottomOffset;
            int l = random.nextInt(16);
            return blockPos.offset(j, k, l);
        });
    }
}

