package net.minecraft.network.protocol.handshake;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
	private int protocolVersion;
	private String hostName;
	private int port;
	private ConnectionProtocol intention;

	public ClientIntentionPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ClientIntentionPacket(String string, int i, ConnectionProtocol connectionProtocol) {
		this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
		this.hostName = string;
		this.port = i;
		this.intention = connectionProtocol;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.protocolVersion = friendlyByteBuf.readVarInt();
		this.hostName = friendlyByteBuf.readUtf(255);
		this.port = friendlyByteBuf.readUnsignedShort();
		this.intention = ConnectionProtocol.getById(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
}
