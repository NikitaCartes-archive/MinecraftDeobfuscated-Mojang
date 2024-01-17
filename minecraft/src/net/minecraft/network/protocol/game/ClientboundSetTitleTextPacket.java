package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetTitleTextPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetTitleTextPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetTitleTextPacket::write, ClientboundSetTitleTextPacket::new
	);
	private final Component text;

	public ClientboundSetTitleTextPacket(Component component) {
		this.text = component;
	}

	private ClientboundSetTitleTextPacket(FriendlyByteBuf friendlyByteBuf) {
		this.text = friendlyByteBuf.readComponentTrusted();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.text);
	}

	@Override
	public PacketType<ClientboundSetTitleTextPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_TITLE_TEXT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.setTitleText(this);
	}

	public Component getText() {
		return this.text;
	}
}
