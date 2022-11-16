/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry.events;

import java.time.Duration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.TelemetryEventProducer;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldLoadTimesEvent
implements TelemetryEventProducer {
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadDuration;

    public WorldLoadTimesEvent(boolean bl, @Nullable Duration duration) {
        this.worldLoadDuration = duration;
        this.newWorld = bl;
    }

    @Override
    public void send(TelemetryEventSender telemetryEventSender) {
        if (this.worldLoadDuration != null) {
            telemetryEventSender.send(TelemetryEventType.WORLD_LOAD_TIMES, builder -> {
                builder.put(TelemetryProperty.WORLD_LOAD_TIME_MS, (int)this.worldLoadDuration.toMillis());
                builder.put(TelemetryProperty.NEW_WORLD, this.newWorld);
            });
        }
    }
}

