/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.VerticalDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRangeDecorator
extends VerticalDecorator<RangeDecoratorConfiguration> {
    private static final Logger LOGGER = LogManager.getLogger();

    public AbstractRangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int y(DecorationContext decorationContext, Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, int i) {
        int k;
        int j = rangeDecoratorConfiguration.bottomInclusive().resolveY(decorationContext);
        if (j >= (k = rangeDecoratorConfiguration.topInclusive().resolveY(decorationContext))) {
            LOGGER.warn("Empty range decorator: {} [{}-{}]", (Object)this, (Object)j, (Object)k);
            return j;
        }
        return this.y(random, j, k);
    }

    protected abstract int y(Random var1, int var2, int var3);
}

