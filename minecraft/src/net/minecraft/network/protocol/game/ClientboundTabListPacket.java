package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundTabListPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundTabListPacket> STREAM_CODEC = Packet.codec(
		ClientboundTabListPacket::write, ClientboundTabListPacket::new
	);
	private final Component header;
	private final Component footer;

	public ClientboundTabListPacket(Component component, Component component2) {
		this.header = component;
		this.footer = component2;
	}

	private ClientboundTabListPacket(FriendlyByteBuf friendlyByteBuf) {
		this.header = friendlyByteBuf.readComponentTrusted();
		this.footer = friendlyByteBuf.readComponentTrusted();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.header);
		friendlyByteBuf.writeComponent(this.footer);
	}

	@Override
	public PacketType<ClientboundTabListPacket> type() {
		return GamePacketTypes.CLIENTBOUND_TAB_LIST;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTabListCustomisation(this);
	}

	public Component getHeader() {
		return this.header;
	}

	public Component getFooter() {
		return this.footer;
	}
}
