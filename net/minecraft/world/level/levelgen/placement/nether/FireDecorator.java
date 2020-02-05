/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class FireDecorator
extends SimpleFeatureDecorator<FrequencyDecoratorConfiguration> {
    public FireDecorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        for (int i = 0; i < random.nextInt(random.nextInt(frequencyDecoratorConfiguration.count) + 1) + 1; ++i) {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = random.nextInt(120) + 4;
            list.add(new BlockPos(j, l, k));
        }
        return list.stream();
    }
}

