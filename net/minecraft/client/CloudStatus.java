/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum CloudStatus {
    OFF("options.off"),
    FAST("options.clouds.fast"),
    FANCY("options.clouds.fancy");

    private final String key;

    private CloudStatus(String string2) {
        this.key = string2;
    }

    public String getKey() {
        return this.key;
    }
}

