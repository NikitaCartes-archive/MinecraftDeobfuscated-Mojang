package net.minecraft.network.chat;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record SignedMessageBody(Component content, Instant timeStamp, long salt, List<SignedMessageBody.LastSeen> lastSeen) {
	private static final byte HASH_SEPARATOR_BYTE = 70;

	public SignedMessageBody(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readComponent(),
			friendlyByteBuf.readInstant(),
			friendlyByteBuf.readLong(),
			friendlyByteBuf.readCollection(ArrayList::new, SignedMessageBody.LastSeen::new)
		);
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.content);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		friendlyByteBuf.writeCollection(this.lastSeen, (friendlyByteBufx, lastSeen) -> lastSeen.write(friendlyByteBufx));
	}

	public HashCode hash() {
		byte[] bs = encodeContent(this.content);
		byte[] cs = encodeLastSeen(this.lastSeen);
		byte[] ds = new byte[16 + bs.length];
		ByteBuffer byteBuffer = ByteBuffer.wrap(ds).order(ByteOrder.BIG_ENDIAN);
		byteBuffer.putLong(this.salt);
		byteBuffer.putLong(this.timeStamp.getEpochSecond());
		byteBuffer.put(bs);
		byteBuffer.put(cs);
		return Hashing.sha256().hashBytes(ds);
	}

	private static byte[] encodeContent(Component component) {
		String string = Component.Serializer.toStableJson(component);
		return string.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] encodeLastSeen(List<SignedMessageBody.LastSeen> list) {
		int i = list.stream().mapToInt(lastSeenx -> 17 + lastSeenx.lastSignature().bytes().length).sum();
		byte[] bs = new byte[i];
		ByteBuffer byteBuffer = ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN);

		for (SignedMessageBody.LastSeen lastSeen : list) {
			UUID uUID = lastSeen.profileId();
			MessageSignature messageSignature = lastSeen.lastSignature();
			byteBuffer.put((byte)70).putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits()).put(messageSignature.bytes());
		}

		return bs;
	}

	public static record LastSeen(UUID profileId, MessageSignature lastSignature) {
		public LastSeen(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readUUID(), new MessageSignature(friendlyByteBuf));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUUID(this.profileId);
			this.lastSignature.write(friendlyByteBuf);
		}
	}
}
