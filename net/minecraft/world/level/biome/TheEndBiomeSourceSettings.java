/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.BiomeSourceSettings;
import net.minecraft.world.level.storage.LevelData;

public class TheEndBiomeSourceSettings
implements BiomeSourceSettings {
    private final long seed;

    public TheEndBiomeSourceSettings(LevelData levelData) {
        this.seed = levelData.getSeed();
    }

    public long getSeed() {
        return this.seed;
    }
}

