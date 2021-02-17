/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfiguration;
import net.minecraft.world.level.levelgen.placement.VerticalDecorator;

public class DepthAverageDecorator
extends VerticalDecorator<DepthAverageConfiguration> {
    public DepthAverageDecorator(Codec<DepthAverageConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int y(DecorationContext decorationContext, Random random, DepthAverageConfiguration depthAverageConfiguration, int i) {
        int j = depthAverageConfiguration.spread();
        return random.nextInt(j) + random.nextInt(j) - j + depthAverageConfiguration.baseline().resolveY(decorationContext);
    }
}

