package net.minecraft.network.protocol.handshake;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention) implements Packet<ServerHandshakePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientIntentionPacket> STREAM_CODEC = Packet.codec(ClientIntentionPacket::write, ClientIntentionPacket::new);
	private static final int MAX_HOST_LENGTH = 255;

	@Deprecated
	public ClientIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention) {
		this.protocolVersion = protocolVersion;
		this.hostName = hostName;
		this.port = port;
		this.intention = intention;
	}

	private ClientIntentionPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readUtf(255), friendlyByteBuf.readUnsignedShort(), ClientIntent.byId(friendlyByteBuf.readVarInt()));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.protocolVersion);
		friendlyByteBuf.writeUtf(this.hostName);
		friendlyByteBuf.writeShort(this.port);
		friendlyByteBuf.writeVarInt(this.intention.id());
	}

	@Override
	public PacketType<ClientIntentionPacket> type() {
		return HandshakePacketTypes.CLIENT_INTENTION;
	}

	public void handle(ServerHandshakePacketListener serverHandshakePacketListener) {
		serverHandshakePacketListener.handleIntention(this);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
