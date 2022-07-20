package net.minecraft.network.chat;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;

public record SignedMessageBody(ChatMessageContent content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
	public static final byte HASH_SEPARATOR_BYTE = 70;

	public SignedMessageBody(FriendlyByteBuf friendlyByteBuf) {
		this(ChatMessageContent.read(friendlyByteBuf), friendlyByteBuf.readInstant(), friendlyByteBuf.readLong(), new LastSeenMessages(friendlyByteBuf));
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		ChatMessageContent.write(friendlyByteBuf, this.content);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		this.lastSeen.write(friendlyByteBuf);
	}

	public HashCode hash() {
		HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha256(), OutputStream.nullOutputStream());

		try {
			DataOutputStream dataOutputStream = new DataOutputStream(hashingOutputStream);
			dataOutputStream.writeLong(this.salt);
			dataOutputStream.writeLong(this.timeStamp.getEpochSecond());
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(dataOutputStream, StandardCharsets.UTF_8);
			outputStreamWriter.write(this.content.plain());
			outputStreamWriter.flush();
			dataOutputStream.write(70);
			if (this.content.isDecorated()) {
				outputStreamWriter.write(Component.Serializer.toStableJson(this.content.decorated()));
				outputStreamWriter.flush();
			}

			this.lastSeen.updateHash(dataOutputStream);
		} catch (IOException var4) {
		}

		return hashingOutputStream.hash();
	}

	public SignedMessageBody withContent(ChatMessageContent chatMessageContent) {
		return new SignedMessageBody(chatMessageContent, this.timeStamp, this.salt, this.lastSeen);
	}
}
