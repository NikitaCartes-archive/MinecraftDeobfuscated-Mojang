package net.minecraft.network.protocol.game;

import java.time.Duration;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import org.apache.commons.lang3.StringUtils;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
	private static final int MAX_MESSAGE_LENGTH = 256;
	public static final Duration MESSAGE_EXPIRES_AFTER = Duration.ofMinutes(2L);
	private final Instant timeStamp;
	private final String message;
	private final Crypt.SaltSignaturePair saltSignature;

	public ServerboundChatPacket(Instant instant, String string, Crypt.SaltSignaturePair saltSignaturePair) {
		this.timeStamp = instant;
		this.message = trimMessage(string);
		this.saltSignature = saltSignaturePair;
	}

	public ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this.timeStamp = Instant.ofEpochSecond(friendlyByteBuf.readLong());
		this.message = friendlyByteBuf.readUtf(256);
		this.saltSignature = new Crypt.SaltSignaturePair(friendlyByteBuf);
	}

	private static String trimMessage(String string) {
		return string.length() > 256 ? string.substring(0, 256) : string;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.timeStamp.getEpochSecond());
		friendlyByteBuf.writeUtf(this.message);
		this.saltSignature.write(friendlyByteBuf);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChat(this);
	}

	public Instant getTimeStamp() {
		return this.timeStamp;
	}

	public String getMessage() {
		return this.message;
	}

	public String getMessageNormalized() {
		return StringUtils.normalizeSpace(this.message);
	}

	public Crypt.SaltSignaturePair getSaltSignature() {
		return this.saltSignature;
	}

	private Instant getExpiresAt() {
		return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
	}

	public boolean hasExpired(Instant instant) {
		return instant.isAfter(this.getExpiresAt());
	}
}
