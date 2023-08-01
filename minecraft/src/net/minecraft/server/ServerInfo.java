package net.minecraft.server;

public interface ServerInfo {
	String getMotd();

	String getServerVersion();

	int getPlayerCount();

	int getMaxPlayers();
}
