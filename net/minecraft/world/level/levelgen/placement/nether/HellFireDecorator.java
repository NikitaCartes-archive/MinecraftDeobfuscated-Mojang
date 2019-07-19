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
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class HellFireDecorator
extends SimpleFeatureDecorator<DecoratorFrequency> {
    public HellFireDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, DecoratorFrequency decoratorFrequency, BlockPos blockPos) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        for (int i = 0; i < random.nextInt(random.nextInt(decoratorFrequency.count) + 1) + 1; ++i) {
            int j = random.nextInt(16);
            int k = random.nextInt(120) + 4;
            int l = random.nextInt(16);
            list.add(blockPos.offset(j, k, l));
        }
        return list.stream();
    }
}

