package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetActionBarTextPacket implements Packet<ClientGamePacketListener> {
	private final Component text;

	public ClientboundSetActionBarTextPacket(Component component) {
		this.text = component;
	}

	public ClientboundSetActionBarTextPacket(FriendlyByteBuf friendlyByteBuf) {
		this.text = friendlyByteBuf.readComponent();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.text);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.setActionBarText(this);
	}

	@Environment(EnvType.CLIENT)
	public Component getText() {
		return this.text;
	}
}