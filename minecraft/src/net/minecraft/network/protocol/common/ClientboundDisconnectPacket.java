package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundDisconnectPacket implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundDisconnectPacket> STREAM_CODEC = Packet.codec(
		ClientboundDisconnectPacket::write, ClientboundDisconnectPacket::new
	);
	private final Component reason;

	public ClientboundDisconnectPacket(Component component) {
		this.reason = component;
	}

	private ClientboundDisconnectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.reason = friendlyByteBuf.readComponentTrusted();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.reason);
	}

	@Override
	public PacketType<ClientboundDisconnectPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_DISCONNECT;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleDisconnect(this);
	}

	public Component getReason() {
		return this.reason;
	}
}
