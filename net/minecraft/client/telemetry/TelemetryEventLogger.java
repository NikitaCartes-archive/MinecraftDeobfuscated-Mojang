/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventInstance;

@Environment(value=EnvType.CLIENT)
public interface TelemetryEventLogger {
    public void log(TelemetryEventInstance var1);
}

