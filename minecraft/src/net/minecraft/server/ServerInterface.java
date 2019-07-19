package net.minecraft.server;

import net.minecraft.server.dedicated.DedicatedServerProperties;

public interface ServerInterface {
	DedicatedServerProperties getProperties();

	String getServerIp();

	int getServerPort();

	String getServerName();

	String getServerVersion();

	int getPlayerCount();

	int getMaxPlayers();

	String[] getPlayerNames();

	String getLevelIdName();

	String getPluginNames();

	String runCommand(String string);

	boolean isDebugging();

	void info(String string);

	void warn(String string);

	void error(String string);

	void debug(String string);
}
