/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class NetherGeneratorSettings
extends ChunkGeneratorSettings {
    @Override
    public int getBedrockFloorPosition() {
        return 0;
    }

    @Override
    public int getBedrockRoofPosition() {
        return 127;
    }
}

