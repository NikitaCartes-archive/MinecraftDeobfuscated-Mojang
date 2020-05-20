/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class CountVeryBiasedRangeDecorator
extends SimpleFeatureDecorator<CountRangeDecoratorConfiguration> {
    public CountVeryBiasedRangeDecorator(Codec<CountRangeDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> place(Random random, CountRangeDecoratorConfiguration countRangeDecoratorConfiguration, BlockPos blockPos) {
        return IntStream.range(0, countRangeDecoratorConfiguration.count).mapToObj(i -> {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = random.nextInt(random.nextInt(random.nextInt(countRangeDecoratorConfiguration.maximum - countRangeDecoratorConfiguration.topOffset) + countRangeDecoratorConfiguration.bottomOffset) + countRangeDecoratorConfiguration.bottomOffset);
            return new BlockPos(j, l, k);
        });
    }
}

