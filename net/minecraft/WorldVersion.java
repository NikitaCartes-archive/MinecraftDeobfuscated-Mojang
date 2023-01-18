/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import java.util.Date;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion {
    public DataVersion getDataVersion();

    public String getId();

    public String getName();

    public int getProtocolVersion();

    public int getPackVersion(PackType var1);

    public Date getBuildTime();

    public boolean isStable();
}

