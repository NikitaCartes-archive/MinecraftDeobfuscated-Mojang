package net.minecraft.network.chat;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;

public record MessageSigner(UUID profileId, Instant timeStamp, long salt) {
	public MessageSigner(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUUID(), friendlyByteBuf.readInstant(), friendlyByteBuf.readLong());
	}

	public static MessageSigner create(UUID uUID) {
		return new MessageSigner(uUID, Instant.now(), Crypt.SaltSupplier.getLong());
	}

	public static MessageSigner system() {
		return create(Util.NIL_UUID);
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.profileId);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
	}

	public boolean isSystem() {
		return this.profileId.equals(Util.NIL_UUID);
	}
}
