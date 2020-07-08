/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.BaseHeightmapDecorator;

public class TopSolidHeightMapDecorator
extends BaseHeightmapDecorator<NoneDecoratorConfiguration> {
    public TopSolidHeightMapDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    protected Heightmap.Types type(NoneDecoratorConfiguration noneDecoratorConfiguration) {
        return Heightmap.Types.OCEAN_FLOOR_WG;
    }
}

