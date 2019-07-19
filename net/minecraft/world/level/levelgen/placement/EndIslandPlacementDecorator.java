/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class EndIslandPlacementDecorator
extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EndIslandPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
        super(function);
    }

    @Override
    public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        Stream<BlockPos> stream = Stream.empty();
        if (random.nextInt(14) == 0) {
            stream = Stream.concat(stream, Stream.of(blockPos.offset(random.nextInt(16), 55 + random.nextInt(16), random.nextInt(16))));
            if (random.nextInt(4) == 0) {
                stream = Stream.concat(stream, Stream.of(blockPos.offset(random.nextInt(16), 55 + random.nextInt(16), random.nextInt(16))));
            }
            return stream;
        }
        return Stream.empty();
    }
}

