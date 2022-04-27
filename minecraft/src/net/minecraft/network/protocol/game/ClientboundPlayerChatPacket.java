package net.minecraft.network.protocol.game;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ClientboundPlayerChatPacket(Component content, ChatType type, ChatSender sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature)
	implements Packet<ClientGamePacketListener> {
	private static final Duration MESSAGE_EXPIRES_AFTER = ServerboundChatPacket.MESSAGE_EXPIRES_AFTER.plus(Duration.ofMinutes(2L));

	public ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readComponent(),
			ChatType.getForIndex(friendlyByteBuf.readByte()),
			new ChatSender(friendlyByteBuf),
			Instant.ofEpochSecond(friendlyByteBuf.readLong()),
			new Crypt.SaltSignaturePair(friendlyByteBuf)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.content);
		friendlyByteBuf.writeByte(this.type.getIndex());
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

	public boolean isSignatureValid(ProfilePublicKey.Trusted trusted) {
		try {
			Signature signature = trusted.verifySignature();
			Crypt.updateChatSignature(signature, this.saltSignature.salt(), this.sender.uuid(), this.timeStamp, this.content.getString());
			return signature.verify(this.saltSignature.signature());
		} catch (CryptException | GeneralSecurityException var3) {
			return false;
		}
	}

	private Instant getExpiresAt() {
		return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
	}

	public boolean hasExpired(Instant instant) {
		return instant.isAfter(this.getExpiresAt());
	}
}
