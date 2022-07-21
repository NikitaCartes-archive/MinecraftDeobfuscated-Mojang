package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
	private final Optional<Component> motd;
	private final Optional<String> iconBase64;
	private final boolean previewsChat;
	private final boolean enforcesSecureChat;

	public ClientboundServerDataPacket(@Nullable Component component, @Nullable String string, boolean bl, boolean bl2) {
		this.motd = Optional.ofNullable(component);
		this.iconBase64 = Optional.ofNullable(string);
		this.previewsChat = bl;
		this.enforcesSecureChat = bl2;
	}

	public ClientboundServerDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.motd = friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent);
		this.iconBase64 = friendlyByteBuf.readOptional(FriendlyByteBuf::readUtf);
		this.previewsChat = friendlyByteBuf.readBoolean();
		this.enforcesSecureChat = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeOptional(this.motd, FriendlyByteBuf::writeComponent);
		friendlyByteBuf.writeOptional(this.iconBase64, FriendlyByteBuf::writeUtf);
		friendlyByteBuf.writeBoolean(this.previewsChat);
		friendlyByteBuf.writeBoolean(this.enforcesSecureChat);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleServerData(this);
	}

	public Optional<Component> getMotd() {
		return this.motd;
	}

	public Optional<String> getIconBase64() {
		return this.iconBase64;
	}

	public boolean previewsChat() {
		return this.previewsChat;
	}

	public boolean enforcesSecureChat() {
		return this.enforcesSecureChat;
	}
}
