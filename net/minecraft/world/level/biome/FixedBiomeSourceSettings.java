/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSourceSettings;
import net.minecraft.world.level.biome.Biomes;

public class FixedBiomeSourceSettings
implements BiomeSourceSettings {
    private Biome biome = Biomes.PLAINS;

    public FixedBiomeSourceSettings setBiome(Biome biome) {
        this.biome = biome;
        return this;
    }

    public Biome getBiome() {
        return this.biome;
    }
}

