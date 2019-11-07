package net.minecraft.client;

import com.mojang.bridge.game.GameSession;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class Session implements GameSession {
	private final int players;
	private final boolean isRemoteServer;
	private final String difficulty;
	private final String gameMode;
	private final UUID id;

	public Session(ClientLevel clientLevel, LocalPlayer localPlayer, ClientPacketListener clientPacketListener) {
		this.players = clientPacketListener.getOnlinePlayers().size();
		this.isRemoteServer = !clientPacketListener.getConnection().isMemoryConnection();
		this.difficulty = clientLevel.getDifficulty().getKey();
		PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(localPlayer.getUUID());
		if (playerInfo != null) {
			this.gameMode = playerInfo.getGameMode().getName();
		} else {
			this.gameMode = "unknown";
		}

		this.id = clientPacketListener.getId();
	}

	@Override
	public int getPlayerCount() {
		return this.players;
	}

	@Override
	public boolean isRemoteServer() {
		return this.isRemoteServer;
	}

	@Override
	public String getDifficulty() {
		return this.difficulty;
	}

	@Override
	public String getGameMode() {
		return this.gameMode;
	}

	@Override
	public UUID getSessionId() {
		return this.id;
	}
}
