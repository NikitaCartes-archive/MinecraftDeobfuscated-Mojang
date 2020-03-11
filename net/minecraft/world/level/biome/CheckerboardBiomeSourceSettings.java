/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSourceSettings;
import net.minecraft.world.level.biome.Biomes;

public class CheckerboardBiomeSourceSettings
implements BiomeSourceSettings {
    private Biome[] allowedBiomes = new Biome[]{Biomes.PLAINS};
    private int size = 1;

    public CheckerboardBiomeSourceSettings(long l) {
    }

    public CheckerboardBiomeSourceSettings setAllowedBiomes(Biome[] biomes) {
        this.allowedBiomes = biomes;
        return this;
    }

    public CheckerboardBiomeSourceSettings setSize(int i) {
        this.size = i;
        return this;
    }

    public Biome[] getAllowedBiomes() {
        return this.allowedBiomes;
    }

    public int getSize() {
        return this.size;
    }
}

