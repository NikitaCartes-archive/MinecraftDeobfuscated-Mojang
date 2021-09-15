/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.mojang.bridge.game.GameVersion;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion
extends GameVersion {
    @Override
    @Deprecated
    default public int getWorldVersion() {
        return this.getDataVersion().getVersion();
    }

    @Override
    @Deprecated
    default public String getSeriesId() {
        return this.getDataVersion().getSeries();
    }

    public DataVersion getDataVersion();
}

