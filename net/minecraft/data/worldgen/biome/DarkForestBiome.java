/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;

public final class DarkForestBiome
extends Biome {
    public DarkForestBiome(Biome.BiomeBuilder biomeBuilder) {
        super(biomeBuilder);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getGrassColor(double d, double e) {
        int i = super.getGrassColor(d, e);
        return (i & 0xFEFEFE) + 2634762 >> 1;
    }
}

