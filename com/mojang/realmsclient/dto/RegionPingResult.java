/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.mojang.realmsclient.dto.ValueObject;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RegionPingResult
extends ValueObject {
    private final String regionName;
    private final int ping;

    public RegionPingResult(String string, int i) {
        this.regionName = string;
        this.ping = i;
    }

    public int ping() {
        return this.ping;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, Float.valueOf(this.ping));
    }
}

