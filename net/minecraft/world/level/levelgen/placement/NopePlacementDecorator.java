/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class NopePlacementDecorator
extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public NopePlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        return Stream.of(blockPos);
    }
}

