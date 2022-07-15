/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;

public record LastSeenMessages(List<Entry> entries) {
    public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 5;

    public LastSeenMessages(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 5), Entry::new));
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)friendlyByteBuf));
    }

    public void updateHash(DataOutput dataOutput) throws IOException {
        for (Entry entry : this.entries) {
            UUID uUID = entry.profileId();
            MessageSignature messageSignature = entry.lastSignature();
            dataOutput.writeByte(70);
            dataOutput.writeLong(uUID.getMostSignificantBits());
            dataOutput.writeLong(uUID.getLeastSignificantBits());
            dataOutput.write(messageSignature.bytes());
        }
    }

    public record Entry(UUID profileId, MessageSignature lastSignature) {
        public Entry(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readUUID(), new MessageSignature(friendlyByteBuf));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUUID(this.profileId);
            this.lastSignature.write(friendlyByteBuf);
        }
    }

    public record Update(LastSeenMessages lastSeen, Optional<Entry> lastReceived) {
        public Update(FriendlyByteBuf friendlyByteBuf) {
            this(new LastSeenMessages(friendlyByteBuf), friendlyByteBuf.readOptional(Entry::new));
        }

        public void write(FriendlyByteBuf friendlyByteBuf2) {
            this.lastSeen.write(friendlyByteBuf2);
            friendlyByteBuf2.writeOptional(this.lastReceived, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)friendlyByteBuf));
        }
    }
}

