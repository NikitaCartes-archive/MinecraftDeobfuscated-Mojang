/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class NetherGeneratorSettings
extends NoiseGeneratorSettings {
    public NetherGeneratorSettings(ChunkGeneratorSettings chunkGeneratorSettings) {
        super(chunkGeneratorSettings);
        chunkGeneratorSettings.ruinedPortalSpacing = 25;
        chunkGeneratorSettings.ruinedPortalSeparation = 10;
    }

    @Override
    public int getBedrockFloorPosition() {
        return 0;
    }

    @Override
    public int getBedrockRoofPosition() {
        return 127;
    }
}

