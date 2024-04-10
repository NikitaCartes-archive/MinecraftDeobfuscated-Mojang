package net.minecraft.util.profiling.jfr;

import net.minecraft.server.MinecraftServer;

public enum Environment {
	CLIENT("client"),
	SERVER("server");

	private final String description;

	private Environment(final String string2) {
		this.description = string2;
	}

	public static Environment from(MinecraftServer minecraftServer) {
		return minecraftServer.isDedicatedServer() ? SERVER : CLIENT;
	}

	public String getDescription() {
		return this.description;
	}
}
