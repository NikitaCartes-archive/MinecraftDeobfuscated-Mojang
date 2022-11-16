/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.client.telemetry.events.TelemetryEventProducer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldLoadEvent
implements TelemetryEventProducer {
    private final WorldLoadEventCallbacks callbacks;
    private boolean eventSent;
    @Nullable
    private TelemetryProperty.GameMode gameMode = null;
    @Nullable
    private String serverBrand;

    public WorldLoadEvent(WorldLoadEventCallbacks worldLoadEventCallbacks) {
        this.callbacks = worldLoadEventCallbacks;
    }

    public void addProperties(TelemetryPropertyMap.Builder builder) {
        if (this.serverBrand != null) {
            builder.put(TelemetryProperty.SERVER_MODDED, !this.serverBrand.equals("vanilla"));
        }
        builder.put(TelemetryProperty.SERVER_TYPE, this.getServerType());
    }

    private TelemetryProperty.ServerType getServerType() {
        if (Minecraft.getInstance().isConnectedToRealms()) {
            return TelemetryProperty.ServerType.REALM;
        }
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return TelemetryProperty.ServerType.LOCAL;
        }
        return TelemetryProperty.ServerType.OTHER;
    }

    @Override
    public void send(TelemetryEventSender telemetryEventSender) {
        if (this.eventSent || this.gameMode == null) {
            return;
        }
        this.eventSent = true;
        this.callbacks.onWorldLoadSent();
        telemetryEventSender.send(TelemetryEventType.WORLD_LOADED, builder -> builder.put(TelemetryProperty.GAME_MODE, this.gameMode));
    }

    public void setGameMode(GameType gameType, boolean bl) {
        this.gameMode = switch (gameType) {
            default -> throw new IncompatibleClassChangeError();
            case GameType.SURVIVAL -> {
                if (bl) {
                    yield TelemetryProperty.GameMode.HARDCORE;
                }
                yield TelemetryProperty.GameMode.SURVIVAL;
            }
            case GameType.CREATIVE -> TelemetryProperty.GameMode.CREATIVE;
            case GameType.ADVENTURE -> TelemetryProperty.GameMode.ADVENTURE;
            case GameType.SPECTATOR -> TelemetryProperty.GameMode.SPECTATOR;
        };
    }

    public void setServerBrand(String string) {
        this.serverBrand = string;
    }

    @Nullable
    public String getServerBrand() {
        return this.serverBrand;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface WorldLoadEventCallbacks {
        public void onWorldLoadSent();
    }
}

