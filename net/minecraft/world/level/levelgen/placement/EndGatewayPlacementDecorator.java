/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.VerticalDecorator;

public class EndGatewayPlacementDecorator
extends VerticalDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected int y(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, int i) {
        return i + 3 + random.nextInt(7);
    }
}

