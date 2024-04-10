package net.minecraft.network;

public enum ConnectionProtocol {
	HANDSHAKING("handshake"),
	PLAY("play"),
	STATUS("status"),
	LOGIN("login"),
	CONFIGURATION("configuration");

	private final String id;

	private ConnectionProtocol(final String string2) {
		this.id = string2;
	}

	public String id() {
		return this.id;
	}
}
