/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class EmeraldPlacementDecorator
extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EmeraldPlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        int i2 = 3 + random.nextInt(6);
        return IntStream.range(0, i2).mapToObj(i -> {
            int j = random.nextInt(16) + blockPos.getX();
            int k = random.nextInt(16) + blockPos.getZ();
            int l = random.nextInt(28) + 4;
            return new BlockPos(j, l, k);
        });
    }
}

