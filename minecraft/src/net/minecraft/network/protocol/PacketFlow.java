package net.minecraft.network.protocol;

public enum PacketFlow {
	SERVERBOUND("serverbound"),
	CLIENTBOUND("clientbound");

	private final String id;

	private PacketFlow(final String string2) {
		this.id = string2;
	}

	public PacketFlow getOpposite() {
		return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
	}

	public String id() {
		return this.id;
	}
}
