/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;

@Environment(value=EnvType.CLIENT)
public interface TelemetryEventProducer {
    public void send(TelemetryEventSender var1);
}

