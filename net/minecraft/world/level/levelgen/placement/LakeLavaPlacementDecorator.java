/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class LakeLavaPlacementDecorator
extends RepeatingDecorator<ChanceDecoratorConfiguration> {
    public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int count(Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (blockPos.getY() < 63 || random.nextInt(10) == 0) {
            return 1;
        }
        return 0;
    }
}

