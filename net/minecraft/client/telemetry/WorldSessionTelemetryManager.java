/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry;

import java.time.Duration;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.client.telemetry.events.PerformanceMetricsEvent;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.client.telemetry.events.WorldLoadTimesEvent;
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldSessionTelemetryManager {
    private final UUID worldSessionId = UUID.randomUUID();
    private final TelemetryEventSender eventSender;
    private final WorldLoadEvent worldLoadEvent;
    private final WorldUnloadEvent worldUnloadEvent = new WorldUnloadEvent();
    private final PerformanceMetricsEvent performanceMetricsEvent;
    private final WorldLoadTimesEvent worldLoadTimesEvent;

    public WorldSessionTelemetryManager(TelemetryEventSender telemetryEventSender, boolean bl, @Nullable Duration duration) {
        this.worldLoadEvent = new WorldLoadEvent(this::worldSessionStart);
        this.performanceMetricsEvent = new PerformanceMetricsEvent(telemetryEventSender);
        this.worldLoadTimesEvent = new WorldLoadTimesEvent(bl, duration);
        this.eventSender = telemetryEventSender.decorate(builder -> {
            this.worldLoadEvent.addProperties((TelemetryPropertyMap.Builder)builder);
            builder.put(TelemetryProperty.WORLD_SESSION_ID, this.worldSessionId);
        });
    }

    public void tick() {
        this.performanceMetricsEvent.tick();
    }

    public void onPlayerInfoReceived(GameType gameType, boolean bl) {
        this.worldLoadEvent.setGameMode(gameType, bl);
        if (this.worldLoadEvent.getServerBrand() != null) {
            this.worldLoadEvent.send(this.eventSender);
        }
    }

    public void onServerBrandReceived(String string) {
        this.worldLoadEvent.setServerBrand(string);
        this.worldLoadEvent.send(this.eventSender);
    }

    public void setTime(long l) {
        this.worldUnloadEvent.setTime(l);
    }

    public void worldSessionStart() {
        this.worldLoadTimesEvent.send(this.eventSender);
        this.worldUnloadEvent.loadedWorld();
        this.performanceMetricsEvent.start();
    }

    public void onDisconnect() {
        this.worldLoadEvent.send(this.eventSender);
        this.performanceMetricsEvent.stop();
        this.worldUnloadEvent.send(this.eventSender);
    }
}

