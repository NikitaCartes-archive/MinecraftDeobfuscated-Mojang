package net.minecraft.server.packs;

public enum PackType {
	CLIENT_RESOURCES("assets"),
	SERVER_DATA("data");

	private final String directory;

	private PackType(final String string2) {
		this.directory = string2;
	}

	public String getDirectory() {
		return this.directory;
	}
}
