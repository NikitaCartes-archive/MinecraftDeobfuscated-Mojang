/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {
    public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

    public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
        output.update(Ints.toByteArray(this.entries.size()));
        for (MessageSignature messageSignature : this.entries) {
            output.update(messageSignature.bytes());
        }
    }

    public Packed pack(MessageSignature.Packer packer) {
        return new Packed(this.entries.stream().map(messageSignature -> messageSignature.pack(packer)).toList());
    }

    public record Packed(List<MessageSignature.Packed> entries) {
        public static final Packed EMPTY = new Packed(List.of());

        public Packed(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 20), MessageSignature.Packed::read));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeCollection(this.entries, MessageSignature.Packed::write);
        }

        public Optional<LastSeenMessages> unpack(MessageSignature.Unpacker unpacker) {
            ArrayList<MessageSignature> list = new ArrayList<MessageSignature>(this.entries.size());
            for (MessageSignature.Packed packed : this.entries) {
                Optional<MessageSignature> optional = packed.unpack(unpacker);
                if (optional.isEmpty()) {
                    return Optional.empty();
                }
                list.add(optional.get());
            }
            return Optional.of(new LastSeenMessages(list));
        }
    }

    public record Update(int offset, BitSet acknowledged) {
        public Update(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readFixedBitSet(20));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.offset);
            friendlyByteBuf.writeFixedBitSet(this.acknowledged, 20);
        }
    }
}

