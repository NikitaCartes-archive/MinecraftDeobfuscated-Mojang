package net.minecraft.network.protocol.common;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundResourcePackPushPacket(UUID id, String url, String hash, boolean required, @Nullable Component prompt)
	implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundResourcePackPushPacket> STREAM_CODEC = Packet.codec(
		ClientboundResourcePackPushPacket::write, ClientboundResourcePackPushPacket::new
	);
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

	private ClientboundResourcePackPushPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUUID(),
			friendlyByteBuf.readUtf(),
			friendlyByteBuf.readUtf(40),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readNullable(FriendlyByteBuf::readComponentTrusted)
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.id);
		friendlyByteBuf.writeUtf(this.url);
		friendlyByteBuf.writeUtf(this.hash);
		friendlyByteBuf.writeBoolean(this.required);
		friendlyByteBuf.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
	}

	@Override
	public PacketType<ClientboundResourcePackPushPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_PUSH;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleResourcePackPush(this);
	}
}
