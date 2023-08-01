package net.minecraft.network.protocol.handshake;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention) implements Packet<ServerHandshakePacketListener> {
	private static final int MAX_HOST_LENGTH = 255;

	@Deprecated
	public ClientIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention) {
		this.protocolVersion = protocolVersion;
		this.hostName = hostName;
		this.port = port;
		this.intention = intention;
	}

	public ClientIntentionPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readUtf(255), friendlyByteBuf.readUnsignedShort(), ClientIntent.byId(friendlyByteBuf.readVarInt()));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.protocolVersion);
		friendlyByteBuf.writeUtf(this.hostName);
		friendlyByteBuf.writeShort(this.port);
		friendlyByteBuf.writeVarInt(this.intention.id());
	}

	public void handle(ServerHandshakePacketListener serverHandshakePacketListener) {
		serverHandshakePacketListener.handleIntention(this);
	}

	@Override
	public ConnectionProtocol nextProtocol() {
		return this.intention.protocol();
	}
}
