/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.BaseHeightmapDecorator;

public class HeightmapDecorator<DC extends DecoratorConfiguration>
extends BaseHeightmapDecorator<DC> {
    public HeightmapDecorator(Codec<DC> codec) {
        super(codec);
    }

    @Override
    protected Heightmap.Types type(DC decoratorConfiguration) {
        return Heightmap.Types.MOTION_BLOCKING;
    }
}

