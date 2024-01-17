package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLoginCompressionPacket implements Packet<ClientLoginPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundLoginCompressionPacket> STREAM_CODEC = Packet.codec(
		ClientboundLoginCompressionPacket::write, ClientboundLoginCompressionPacket::new
	);
	private final int compressionThreshold;

	public ClientboundLoginCompressionPacket(int i) {
		this.compressionThreshold = i;
	}

	private ClientboundLoginCompressionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.compressionThreshold = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.compressionThreshold);
	}

	@Override
	public PacketType<ClientboundLoginCompressionPacket> type() {
		return LoginPacketTypes.CLIENTBOUND_LOGIN_COMPRESSION;
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleCompression(this);
	}

	public int getCompressionThreshold() {
		return this.compressionThreshold;
	}
}
