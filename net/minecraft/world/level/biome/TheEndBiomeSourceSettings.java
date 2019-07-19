/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.BiomeSourceSettings;

public class TheEndBiomeSourceSettings
implements BiomeSourceSettings {
    private long seed;

    public TheEndBiomeSourceSettings setSeed(long l) {
        this.seed = l;
        return this;
    }

    public long getSeed() {
        return this.seed;
    }
}

