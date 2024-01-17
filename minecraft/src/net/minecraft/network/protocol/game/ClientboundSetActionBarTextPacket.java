package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetActionBarTextPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetActionBarTextPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetActionBarTextPacket::write, ClientboundSetActionBarTextPacket::new
	);
	private final Component text;

	public ClientboundSetActionBarTextPacket(Component component) {
		this.text = component;
	}

	private ClientboundSetActionBarTextPacket(FriendlyByteBuf friendlyByteBuf) {
		this.text = friendlyByteBuf.readComponentTrusted();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.text);
	}

	@Override
	public PacketType<ClientboundSetActionBarTextPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_ACTION_BAR_TEXT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.setActionBarText(this);
	}

	public Component getText() {
		return this.text;
	}
}
