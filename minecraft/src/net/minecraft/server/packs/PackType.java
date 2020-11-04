package net.minecraft.server.packs;

import com.mojang.bridge.game.GameVersion;

public enum PackType {
	CLIENT_RESOURCES("assets", com.mojang.bridge.game.PackType.RESOURCE),
	SERVER_DATA("data", com.mojang.bridge.game.PackType.DATA);

	private final String directory;
	private final com.mojang.bridge.game.PackType bridgeType;

	private PackType(String string2, com.mojang.bridge.game.PackType packType) {
		this.directory = string2;
		this.bridgeType = packType;
	}

	public String getDirectory() {
		return this.directory;
	}

	public int getVersion(GameVersion gameVersion) {
		return gameVersion.getPackVersion(this.bridgeType);
	}
}
