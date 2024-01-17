package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetSubtitleTextPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetSubtitleTextPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetSubtitleTextPacket::write, ClientboundSetSubtitleTextPacket::new
	);
	private final Component text;

	public ClientboundSetSubtitleTextPacket(Component component) {
		this.text = component;
	}

	private ClientboundSetSubtitleTextPacket(FriendlyByteBuf friendlyByteBuf) {
		this.text = friendlyByteBuf.readComponentTrusted();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.text);
	}

	@Override
	public PacketType<ClientboundSetSubtitleTextPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_SUBTITLE_TEXT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.setSubtitleText(this);
	}

	public Component getText() {
		return this.text;
	}
}
