package net.minecraft.network.protocol.game;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;

public record ClientboundPlayerChatPacket(Component content, int typeId, ChatSender sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature)
	implements Packet<ClientGamePacketListener> {
	private static final Duration MESSAGE_EXPIRES_AFTER = ServerboundChatPacket.MESSAGE_EXPIRES_AFTER.plus(Duration.ofMinutes(2L));

	public ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readComponent(),
			friendlyByteBuf.readVarInt(),
			new ChatSender(friendlyByteBuf),
			Instant.ofEpochSecond(friendlyByteBuf.readLong()),
			new Crypt.SaltSignaturePair(friendlyByteBuf)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.content);
		friendlyByteBuf.writeVarInt(this.typeId);
		this.sender.write(friendlyByteBuf);
		friendlyByteBuf.writeLong(this.timeStamp.getEpochSecond());
		this.saltSignature.write(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}

	public SignedMessage getSignedMessage() {
		MessageSignature messageSignature = new MessageSignature(this.sender.uuid(), this.timeStamp, this.saltSignature);
		return new SignedMessage(this.content, messageSignature);
	}

	private Instant getExpiresAt() {
		return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
	}

	public boolean hasExpired(Instant instant) {
		return instant.isAfter(this.getExpiresAt());
	}

	public ChatType resolveType(Registry<ChatType> registry) {
		return (ChatType)Objects.requireNonNull(registry.byId(this.typeId), "Invalid chat type");
	}
}
