/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.VerticalDecorator;

public class RangeDecorator
extends VerticalDecorator<RangeDecoratorConfiguration> {
    public RangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int y(DecorationContext decorationContext, Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, int i) {
        return rangeDecoratorConfiguration.height.sample(random, decorationContext);
    }
}

