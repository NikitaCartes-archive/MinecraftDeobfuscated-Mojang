/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecoratorChance;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class ChancePassthroughDecorator
extends SimpleFeatureDecorator<DecoratorChance> {
    public ChancePassthroughDecorator(Function<Dynamic<?>, ? extends DecoratorChance> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, DecoratorChance decoratorChance, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / (float)decoratorChance.chance) {
            return Stream.of(blockPos);
        }
        return Stream.empty();
    }
}

