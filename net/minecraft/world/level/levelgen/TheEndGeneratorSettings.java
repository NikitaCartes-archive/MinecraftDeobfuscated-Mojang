/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class TheEndGeneratorSettings
extends ChunkGeneratorSettings {
    private BlockPos spawnPosition;

    public TheEndGeneratorSettings setSpawnPosition(BlockPos blockPos) {
        this.spawnPosition = blockPos;
        return this;
    }

    public BlockPos getSpawnPosition() {
        return this.spawnPosition;
    }
}

