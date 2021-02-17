/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.AbstractBiasedRangeDecorator;

public class RangeVeryBiasedToBottomDecorator
extends AbstractBiasedRangeDecorator {
    public RangeVeryBiasedToBottomDecorator(Codec<BiasedRangeDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int y(Random random, int i, int j, int k) {
        int l = Mth.nextInt(random, i + k, j);
        int m = Mth.nextInt(random, i, l - 1);
        return Mth.nextInt(random, i, m - 1 + k);
    }
}

