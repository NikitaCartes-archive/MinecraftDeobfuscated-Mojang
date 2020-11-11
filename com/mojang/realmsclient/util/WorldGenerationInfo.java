/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.mojang.realmsclient.util.LevelType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class WorldGenerationInfo {
    private final String seed;
    private final LevelType levelType;
    private final boolean generateStructures;

    public WorldGenerationInfo(String string, LevelType levelType, boolean bl) {
        this.seed = string;
        this.levelType = levelType;
        this.generateStructures = bl;
    }

    public String getSeed() {
        return this.seed;
    }

    public LevelType getLevelType() {
        return this.levelType;
    }

    public boolean shouldGenerateStructures() {
        return this.generateStructures;
    }
}

