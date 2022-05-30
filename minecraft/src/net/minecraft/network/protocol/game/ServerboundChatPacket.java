package net.minecraft.network.protocol.game;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.StringUtil;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
	public static final Duration MESSAGE_EXPIRES_AFTER = Duration.ofMinutes(5L);
	private final String message;
	private final Instant timeStamp;
	private final Crypt.SaltSignaturePair saltSignature;
	private final boolean signedPreview;

	public ServerboundChatPacket(String string, MessageSignature messageSignature, boolean bl) {
		this.message = StringUtil.trimChatMessage(string);
		this.timeStamp = messageSignature.timeStamp();
		this.saltSignature = messageSignature.saltSignature();
		this.signedPreview = bl;
	}

	public ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this.message = friendlyByteBuf.readUtf(256);
		this.timeStamp = friendlyByteBuf.readInstant();
		this.saltSignature = new Crypt.SaltSignaturePair(friendlyByteBuf);
		this.signedPreview = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.message, 256);
		friendlyByteBuf.writeInstant(this.timeStamp);
		Crypt.SaltSignaturePair.write(friendlyByteBuf, this.saltSignature);
		friendlyByteBuf.writeBoolean(this.signedPreview);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChat(this);
	}

	public String getMessage() {
		return this.message;
	}

	public MessageSignature getSignature(UUID uUID) {
		return new MessageSignature(uUID, this.timeStamp, this.saltSignature);
	}

	public Instant getTimeStamp() {
		return this.timeStamp;
	}

	public boolean signedPreview() {
		return this.signedPreview;
	}
}
