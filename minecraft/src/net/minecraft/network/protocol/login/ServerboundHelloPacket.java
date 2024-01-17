package net.minecraft.network.protocol.login;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundHelloPacket(String name, UUID profileId) implements Packet<ServerLoginPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundHelloPacket> STREAM_CODEC = Packet.codec(
		ServerboundHelloPacket::write, ServerboundHelloPacket::new
	);

	private ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUtf(16), friendlyByteBuf.readUUID());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.name, 16);
		friendlyByteBuf.writeUUID(this.profileId);
	}

	@Override
	public PacketType<ServerboundHelloPacket> type() {
		return LoginPacketTypes.SERVERBOUND_HELLO;
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleHello(this);
	}
}
