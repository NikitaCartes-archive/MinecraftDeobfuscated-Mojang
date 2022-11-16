/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryPropertyMap;

@Environment(value=EnvType.CLIENT)
public record TelemetryEventInstance(TelemetryEventType type, TelemetryPropertyMap properties) {
    public static final Codec<TelemetryEventInstance> CODEC = TelemetryEventType.CODEC.dispatchStable(TelemetryEventInstance::type, TelemetryEventType::codec);

    public TelemetryEventInstance {
        telemetryPropertyMap.propertySet().forEach(telemetryProperty -> {
            if (!telemetryEventType.contains(telemetryProperty)) {
                throw new IllegalArgumentException("Property '" + telemetryProperty.id() + "' not expected for event: '" + telemetryEventType.id() + "'");
            }
        });
    }

    public TelemetryEvent export(TelemetrySession telemetrySession) {
        return this.type.export(telemetrySession, this.properties);
    }
}

