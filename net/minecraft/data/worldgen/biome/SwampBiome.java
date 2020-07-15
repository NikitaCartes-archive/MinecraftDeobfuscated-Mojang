/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;

public final class SwampBiome
extends Biome {
    public SwampBiome(Biome.BiomeBuilder biomeBuilder) {
        super(biomeBuilder);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getGrassColor(double d, double e) {
        double f = BIOME_INFO_NOISE.getValue(d * 0.0225, e * 0.0225, false);
        if (f < -0.1) {
            return 5011004;
        }
        return 6975545;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getFoliageColor() {
        return 6975545;
    }
}

