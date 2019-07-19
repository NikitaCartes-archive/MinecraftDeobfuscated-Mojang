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
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class EmeraldPlacementDecorator
extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EmeraldPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        int i2 = 3 + random.nextInt(6);
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16);
            int k = random.nextInt(28) + 4;
            int l = random.nextInt(16);
            return blockPos.offset(j, k, l);
        });
    }
}

