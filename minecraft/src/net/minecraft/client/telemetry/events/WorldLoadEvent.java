package net.minecraft.client.telemetry.events;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class WorldLoadEvent {
	private boolean eventSent;
	@Nullable
	private TelemetryProperty.GameMode gameMode = null;
	@Nullable
	private String serverBrand;

	public void addProperties(TelemetryPropertyMap.Builder builder) {
		if (this.serverBrand != null) {
			builder.put(TelemetryProperty.SERVER_MODDED, !this.serverBrand.equals("vanilla"));
		}

		builder.put(TelemetryProperty.SERVER_TYPE, this.getServerType());
	}

	private TelemetryProperty.ServerType getServerType() {
		if (Minecraft.getInstance().isConnectedToRealms()) {
			return TelemetryProperty.ServerType.REALM;
		} else {
			return Minecraft.getInstance().hasSingleplayerServer() ? TelemetryProperty.ServerType.LOCAL : TelemetryProperty.ServerType.OTHER;
		}
	}

	public boolean send(TelemetryEventSender telemetryEventSender) {
		if (!this.eventSent && this.gameMode != null && this.serverBrand != null) {
			this.eventSent = true;
			telemetryEventSender.send(TelemetryEventType.WORLD_LOADED, builder -> builder.put(TelemetryProperty.GAME_MODE, this.gameMode));
			return true;
		} else {
			return false;
		}
	}

	public void setGameMode(GameType gameType, boolean bl) {
		this.gameMode = switch (gameType) {
			case SURVIVAL -> bl ? TelemetryProperty.GameMode.HARDCORE : TelemetryProperty.GameMode.SURVIVAL;
			case CREATIVE -> TelemetryProperty.GameMode.CREATIVE;
			case ADVENTURE -> TelemetryProperty.GameMode.ADVENTURE;
			case SPECTATOR -> TelemetryProperty.GameMode.SPECTATOR;
		};
	}

	public void setServerBrand(String string) {
		this.serverBrand = string;
	}
}
