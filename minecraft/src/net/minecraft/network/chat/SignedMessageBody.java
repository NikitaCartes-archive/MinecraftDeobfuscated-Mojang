package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
	public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content),
					ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp),
					Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt),
					LastSeenMessages.CODEC.optionalFieldOf("last_seen", LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)
				)
				.apply(instance, SignedMessageBody::new)
	);

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

	public SignedMessageBody.Packed pack(MessageSignatureCache messageSignatureCache) {
		return new SignedMessageBody.Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack(messageSignatureCache));
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

		public Optional<SignedMessageBody> unpack(MessageSignatureCache messageSignatureCache) {
			return this.lastSeen.unpack(messageSignatureCache).map(lastSeenMessages -> new SignedMessageBody(this.content, this.timeStamp, this.salt, lastSeenMessages));
		}
	}
}
