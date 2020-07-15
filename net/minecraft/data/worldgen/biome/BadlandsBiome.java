/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;

public final class BadlandsBiome
extends Biome {
    public BadlandsBiome(Biome.BiomeBuilder biomeBuilder) {
        super(biomeBuilder);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getFoliageColor() {
        return 10387789;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getGrassColor(double d, double e) {
        return 9470285;
    }
}

