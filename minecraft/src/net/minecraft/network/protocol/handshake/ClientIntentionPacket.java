package net.minecraft.network.protocol.handshake;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
	private static final int MAX_HOST_LENGTH = 255;
	private final int protocolVersion;
	private final String hostName;
	private final int port;
	private final ConnectionProtocol intention;

	public ClientIntentionPacket(String string, int i, ConnectionProtocol connectionProtocol) {
		this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
		this.hostName = string;
		this.port = i;
		this.intention = connectionProtocol;
	}

	public ClientIntentionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.protocolVersion = friendlyByteBuf.readVarInt();
		this.hostName = friendlyByteBuf.readUtf(255);
		this.port = friendlyByteBuf.readUnsignedShort();
		this.intention = ConnectionProtocol.getById(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.protocolVersion);
		friendlyByteBuf.writeUtf(this.hostName);
		friendlyByteBuf.writeShort(this.port);
		friendlyByteBuf.writeVarInt(this.intention.getId());
	}

	public void handle(ServerHandshakePacketListener serverHandshakePacketListener) {
		serverHandshakePacketListener.handleIntention(this);
	}

	public ConnectionProtocol getIntention() {
		return this.intention;
	}

	public int getProtocolVersion() {
		return this.protocolVersion;
	}

	public String getHostName() {
		return this.hostName;
	}

	public int getPort() {
		return this.port;
	}
}
