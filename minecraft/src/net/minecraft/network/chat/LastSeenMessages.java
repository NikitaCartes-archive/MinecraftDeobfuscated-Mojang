package net.minecraft.network.chat;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record LastSeenMessages(List<LastSeenMessages.Entry> entries) {
	public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
	public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 5;

	public LastSeenMessages(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 5), LastSeenMessages.Entry::new));
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.entries, (friendlyByteBufx, entry) -> entry.write(friendlyByteBufx));
	}

	public void updateHash(DataOutput dataOutput) throws IOException {
		for (LastSeenMessages.Entry entry : this.entries) {
			UUID uUID = entry.profileId();
			MessageSignature messageSignature = entry.lastSignature();
			dataOutput.writeByte(70);
			dataOutput.writeLong(uUID.getMostSignificantBits());
			dataOutput.writeLong(uUID.getLeastSignificantBits());
			dataOutput.write(messageSignature.bytes());
		}
	}

	public static record Entry(UUID profileId, MessageSignature lastSignature) {
		public Entry(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readUUID(), new MessageSignature(friendlyByteBuf));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUUID(this.profileId);
			this.lastSignature.write(friendlyByteBuf);
		}
	}

	public static record Update(LastSeenMessages lastSeen, Optional<LastSeenMessages.Entry> lastReceived) {
		public Update(FriendlyByteBuf friendlyByteBuf) {
			this(new LastSeenMessages(friendlyByteBuf), friendlyByteBuf.readOptional(LastSeenMessages.Entry::new));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			this.lastSeen.write(friendlyByteBuf);
			friendlyByteBuf.writeOptional(this.lastReceived, (friendlyByteBufx, entry) -> entry.write(friendlyByteBufx));
		}
	}
}
