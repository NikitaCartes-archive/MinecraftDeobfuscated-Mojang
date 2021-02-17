/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class CountWithExtraChanceDecorator
extends RepeatingDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
    public CountWithExtraChanceDecorator(Codec<FrequencyWithExtraChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int count(Random random, FrequencyWithExtraChanceDecoratorConfiguration frequencyWithExtraChanceDecoratorConfiguration, BlockPos blockPos) {
        return frequencyWithExtraChanceDecoratorConfiguration.count + (random.nextFloat() < frequencyWithExtraChanceDecoratorConfiguration.extraChance ? frequencyWithExtraChanceDecoratorConfiguration.extraCount : 0);
    }
}

