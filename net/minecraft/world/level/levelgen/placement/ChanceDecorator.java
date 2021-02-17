/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class ChanceDecorator
extends RepeatingDecorator<ChanceDecoratorConfiguration> {
    public ChanceDecorator(Codec<ChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int count(Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / (float)chanceDecoratorConfiguration.chance) {
            return 1;
        }
        return 0;
    }
}

