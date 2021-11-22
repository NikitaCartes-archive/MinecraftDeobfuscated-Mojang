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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.util.TelemetryConstants;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
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
	private ClientTelemetryManager.PlayerInfo playerInfo;
	@Nullable
	private String serverBrand;

	public ClientTelemetryManager(Minecraft minecraft, UserApiService userApiService, Optional<String> optional, Optional<String> optional2, UUID uUID) {
		this.minecraft = minecraft;
		if (!SharedConstants.IS_RUNNING_IN_IDE) {
			this.telemetrySession = userApiService.newTelemetrySession(EXECUTOR);
			TelemetryPropertyContainer telemetryPropertyContainer = this.telemetrySession.globalProperties();
			addOptionalProperty("UserId", optional, telemetryPropertyContainer);
			addOptionalProperty("ClientId", optional2, telemetryPropertyContainer);
			telemetryPropertyContainer.addProperty("deviceSessionId", uUID.toString());
			telemetryPropertyContainer.addProperty("WorldSessionId", UUID.randomUUID().toString());
			this.telemetrySession
				.eventSetupFunction(
					telemetryPropertyContainerx -> telemetryPropertyContainerx.addProperty("eventTimestampUtc", TelemetryConstants.TIMESTAMP_FORMATTER.format(Instant.now()))
				);
		} else {
			this.telemetrySession = TelemetrySession.DISABLED;
		}
	}

	private static void addOptionalProperty(String string, Optional<String> optional, TelemetryPropertyContainer telemetryPropertyContainer) {
		optional.ifPresentOrElse(string2 -> telemetryPropertyContainer.addProperty(string, string2), () -> telemetryPropertyContainer.addNullProperty(string));
	}

	public void onPlayerInfoReceived(GameType gameType, boolean bl) {
		this.playerInfo = new ClientTelemetryManager.PlayerInfo(gameType, bl);
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

	private void sendWorldLoadEvent(ClientTelemetryManager.PlayerInfo playerInfo) {
		if (!this.worldLoadEventSent) {
			this.worldLoadEventSent = true;
			if (this.telemetrySession.isEnabled()) {
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
		}
	}

	private String getServerType() {
		if (this.minecraft.isConnectedToRealms()) {
			return "realm";
		} else {
			return this.minecraft.hasSingleplayerServer() ? "local" : "server";
		}
	}

	public void onDisconnect() {
		if (this.playerInfo != null) {
			this.sendWorldLoadEvent(this.playerInfo);
		}
	}

	@Environment(EnvType.CLIENT)
	static record PlayerInfo(GameType gameType, boolean hardcore) {
		public int getGameModeId() {
			if (this.hardcore && this.gameType == GameType.SURVIVAL) {
				return 99;
			} else {
				return switch (this.gameType) {
					case SURVIVAL -> 0;
					case CREATIVE -> 1;
					case ADVENTURE -> 2;
					case SPECTATOR -> 6;
				};
			}
		}
	}
}
