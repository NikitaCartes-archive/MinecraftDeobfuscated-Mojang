package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
	private final Component motd;
	private final Optional<byte[]> iconBytes;
	private final boolean enforcesSecureChat;

	public ClientboundServerDataPacket(Component component, Optional<byte[]> optional, boolean bl) {
		this.motd = component;
		this.iconBytes = optional;
		this.enforcesSecureChat = bl;
	}

	public ClientboundServerDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.motd = friendlyByteBuf.readComponentTrusted();
		this.iconBytes = friendlyByteBuf.readOptional(FriendlyByteBuf::readByteArray);
		this.enforcesSecureChat = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.motd);
		friendlyByteBuf.writeOptional(this.iconBytes, FriendlyByteBuf::writeByteArray);
		friendlyByteBuf.writeBoolean(this.enforcesSecureChat);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleServerData(this);
	}

	public Component getMotd() {
		return this.motd;
	}

	public Optional<byte[]> getIconBytes() {
		return this.iconBytes;
	}

	public boolean enforcesSecureChat() {
		return this.enforcesSecureChat;
	}
}
