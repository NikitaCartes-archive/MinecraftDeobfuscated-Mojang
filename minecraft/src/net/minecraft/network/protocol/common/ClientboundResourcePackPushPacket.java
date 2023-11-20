package net.minecraft.network.protocol.common;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPushPacket(UUID id, String url, String hash, boolean required, @Nullable Component prompt)
	implements Packet<ClientCommonPacketListener> {
	public static final int MAX_HASH_LENGTH = 40;

	public ClientboundResourcePackPushPacket(UUID id, String url, String hash, boolean required, @Nullable Component prompt) {
		if (hash.length() > 40) {
			throw new IllegalArgumentException("Hash is too long (max 40, was " + hash.length() + ")");
		} else {
			this.id = id;
			this.url = url;
			this.hash = hash;
			this.required = required;
			this.prompt = prompt;
		}
	}

	public ClientboundResourcePackPushPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUUID(),
			friendlyByteBuf.readUtf(),
			friendlyByteBuf.readUtf(40),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readNullable(FriendlyByteBuf::readComponentTrusted)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.id);
		friendlyByteBuf.writeUtf(this.url);
		friendlyByteBuf.writeUtf(this.hash);
		friendlyByteBuf.writeBoolean(this.required);
		friendlyByteBuf.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleResourcePackPush(this);
	}
}
