/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfigation;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class CountDepthAverageDecorator
extends SimpleFeatureDecorator<DepthAverageConfigation> {
    public CountDepthAverageDecorator(Function<Dynamic<?>, ? extends DepthAverageConfigation> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, DepthAverageConfigation depthAverageConfigation, BlockPos blockPos) {
        int i = depthAverageConfigation.count;
        int j = depthAverageConfigation.baseline;
        int k2 = depthAverageConfigation.spread;
        return IntStream.range(0, i).mapToObj(k -> {
            int l = random.nextInt(16);
            int m = random.nextInt(k2) + random.nextInt(k2) - k2 + j;
            int n = random.nextInt(16);
            return blockPos.offset(l, m, n);
        });
    }
}

