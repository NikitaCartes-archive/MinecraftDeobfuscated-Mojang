/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public abstract class EdgeDecorator<DC extends DecoratorConfiguration>
extends FeatureDecorator<DC> {
    public EdgeDecorator(Codec<DC> codec) {
        super(codec);
    }

    protected abstract Heightmap.Types type(DC var1);
}

