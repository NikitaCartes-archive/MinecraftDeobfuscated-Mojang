/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.util.TelemetryConstants;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientTelemetryManager {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(1);
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Telemetry-Sender-#" + THREAD_COUNT.getAndIncrement());
        return thread;
    });
    private final Minecraft minecraft;
    private final TelemetrySession telemetrySession;
    private boolean worldLoadEventSent;
    @Nullable
    private PlayerInfo playerInfo;
    @Nullable
    private String serverBrand;

    public ClientTelemetryManager(Minecraft minecraft, UserApiService userApiService, Optional<String> optional, Optional<String> optional2, UUID uUID) {
        this.minecraft = minecraft;
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            this.telemetrySession = userApiService.newTelemetrySession(EXECUTOR);
            TelemetryPropertyContainer telemetryPropertyContainer2 = this.telemetrySession.globalProperties();
            ClientTelemetryManager.addOptionalProperty("UserId", optional, telemetryPropertyContainer2);
            ClientTelemetryManager.addOptionalProperty("ClientId", optional2, telemetryPropertyContainer2);
            telemetryPropertyContainer2.addProperty("deviceSessionId", uUID.toString());
            telemetryPropertyContainer2.addProperty("WorldSessionId", UUID.randomUUID().toString());
            this.telemetrySession.eventSetupFunction(telemetryPropertyContainer -> telemetryPropertyContainer.addProperty("eventTimestampUtc", TelemetryConstants.TIMESTAMP_FORMATTER.format(Instant.now())));
        } else {
            this.telemetrySession = TelemetrySession.DISABLED;
        }
    }

    private static void addOptionalProperty(String string, Optional<String> optional, TelemetryPropertyContainer telemetryPropertyContainer) {
        optional.ifPresentOrElse(string2 -> telemetryPropertyContainer.addProperty(string, (String)string2), () -> telemetryPropertyContainer.addNullProperty(string));
    }

    public void onPlayerInfoReceived(GameType gameType, boolean bl) {
        this.playerInfo = new PlayerInfo(gameType, bl);
        if (this.serverBrand != null) {
            this.sendWorldLoadEvent(this.playerInfo);
        }
    }

    public void onServerBrandReceived(String string) {
        this.serverBrand = string;
        if (this.playerInfo != null) {
            this.sendWorldLoadEvent(this.playerInfo);
        }
    }

    private void sendWorldLoadEvent(PlayerInfo playerInfo) {
        if (this.worldLoadEventSent) {
            return;
        }
        this.worldLoadEventSent = true;
        if (!this.telemetrySession.isEnabled()) {
            return;
        }
        TelemetryEvent telemetryEvent = this.telemetrySession.createNewEvent("WorldLoaded");
        WorldVersion worldVersion = SharedConstants.getCurrentVersion();
        telemetryEvent.addProperty("build_display_name", worldVersion.getId());
        telemetryEvent.addProperty("clientModded", Minecraft.checkModStatus().shouldReportAsModified());
        if (this.serverBrand != null) {
            telemetryEvent.addProperty("serverModded", !this.serverBrand.equals("vanilla"));
        } else {
            telemetryEvent.addNullProperty("serverModded");
        }
        telemetryEvent.addProperty("server_type", this.getServerType());
        telemetryEvent.addProperty("BuildPlat", Util.getPlatform().telemetryName());
        telemetryEvent.addProperty("Plat", System.getProperty("os.name"));
        telemetryEvent.addProperty("javaVersion", System.getProperty("java.version"));
        telemetryEvent.addProperty("PlayerGameMode", playerInfo.getGameModeId());
        telemetryEvent.send();
    }

    private String getServerType() {
        if (this.minecraft.isConnectedToRealms()) {
            return "realm";
        }
        if (this.minecraft.hasSingleplayerServer()) {
            return "local";
        }
        return "server";
    }

    public void onDisconnect() {
        if (this.playerInfo != null) {
            this.sendWorldLoadEvent(this.playerInfo);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record PlayerInfo(GameType gameType, boolean hardcore) {
        public int getGameModeId() {
            if (this.hardcore && this.gameType == GameType.SURVIVAL) {
                return 99;
            }
            return switch (this.gameType) {
                default -> throw new IncompatibleClassChangeError();
                case GameType.SURVIVAL -> 0;
                case GameType.CREATIVE -> 1;
                case GameType.ADVENTURE -> 2;
                case GameType.SPECTATOR -> 6;
            };
        }
    }
}

