package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetSubtitleTextPacket implements Packet<ClientGamePacketListener> {
	private final Component text;

	public ClientboundSetSubtitleTextPacket(Component component) {
		this.text = component;
	}

	public ClientboundSetSubtitleTextPacket(FriendlyByteBuf friendlyByteBuf) {
		this.text = friendlyByteBuf.readComponentTrusted();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.text);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.setSubtitleText(this);
	}

	public Component getText() {
		return this.text;
	}
}
