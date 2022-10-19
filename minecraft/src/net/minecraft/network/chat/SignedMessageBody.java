package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
	public static SignedMessageBody unsigned(String string) {
		return new SignedMessageBody(string, Instant.now(), 0L, LastSeenMessages.EMPTY);
	}

	public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
		output.update(Longs.toByteArray(this.salt));
		output.update(Longs.toByteArray(this.timeStamp.getEpochSecond()));
		byte[] bs = this.content.getBytes(StandardCharsets.UTF_8);
		output.update(Ints.toByteArray(bs.length));
		output.update(bs);
		this.lastSeen.updateSignature(output);
	}

	public SignedMessageBody.Packed pack(MessageSignature.Packer packer) {
		return new SignedMessageBody.Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack(packer));
	}

	public static record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
		public Packed(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readUtf(256), friendlyByteBuf.readInstant(), friendlyByteBuf.readLong(), new LastSeenMessages.Packed(friendlyByteBuf));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUtf(this.content, 256);
			friendlyByteBuf.writeInstant(this.timeStamp);
			friendlyByteBuf.writeLong(this.salt);
			this.lastSeen.write(friendlyByteBuf);
		}

		public Optional<SignedMessageBody> unpack(MessageSignature.Unpacker unpacker) {
			return this.lastSeen.unpack(unpacker).map(lastSeenMessages -> new SignedMessageBody(this.content, this.timeStamp, this.salt, lastSeenMessages));
		}
	}
}
