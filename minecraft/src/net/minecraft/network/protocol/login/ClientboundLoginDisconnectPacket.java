package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundLoginDisconnectPacket> STREAM_CODEC = Packet.codec(
		ClientboundLoginDisconnectPacket::write, ClientboundLoginDisconnectPacket::new
	);
	private final Component reason;

	public ClientboundLoginDisconnectPacket(Component component) {
		this.reason = component;
	}

	private ClientboundLoginDisconnectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.reason = Component.Serializer.fromJsonLenient(friendlyByteBuf.readUtf(262144));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(Component.Serializer.toJson(this.reason));
	}

	@Override
	public PacketType<ClientboundLoginDisconnectPacket> type() {
		return LoginPacketTypes.CLIENTBOUND_LOGIN_DISCONNECT;
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleDisconnect(this);
	}

	public Component getReason() {
		return this.reason;
	}
}
