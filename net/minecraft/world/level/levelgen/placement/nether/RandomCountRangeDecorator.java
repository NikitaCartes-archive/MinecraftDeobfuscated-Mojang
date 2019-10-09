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
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class RandomCountRangeDecorator
extends SimpleFeatureDecorator<CountRangeDecoratorConfiguration> {
    public RandomCountRangeDecorator(Function<Dynamic<?>, ? extends CountRangeDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, CountRangeDecoratorConfiguration countRangeDecoratorConfiguration, BlockPos blockPos) {
        int i2 = random.nextInt(Math.max(countRangeDecoratorConfiguration.count, 1));
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = random.nextInt(countRangeDecoratorConfiguration.maximum - countRangeDecoratorConfiguration.topOffset) + countRangeDecoratorConfiguration.bottomOffset;
            return new BlockPos(j, l, k);
        });
    }
}

