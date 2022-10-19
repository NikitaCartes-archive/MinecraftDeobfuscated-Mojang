/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;
import org.jetbrains.annotations.Nullable;

public record MessageSignature(byte[] bytes) {
    public static final int BYTES = 256;

    public MessageSignature {
        Preconditions.checkState(bs.length == 256, "Invalid message signature size");
    }

    public static MessageSignature read(FriendlyByteBuf friendlyByteBuf) {
        byte[] bs = new byte[256];
        friendlyByteBuf.readBytes(bs);
        return new MessageSignature(bs);
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, MessageSignature messageSignature) {
        friendlyByteBuf.writeBytes(messageSignature.bytes);
    }

    public boolean verify(SignatureValidator signatureValidator, SignatureUpdater signatureUpdater) {
        return signatureValidator.validate(signatureUpdater, this.bytes);
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(this.bytes);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MessageSignature)) return false;
        MessageSignature messageSignature = (MessageSignature)object;
        if (!Arrays.equals(this.bytes, messageSignature.bytes)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.bytes);
    }

    public Packed pack(Packer packer) {
        int i = packer.pack(this);
        return i != -1 ? new Packed(i) : new Packed(this);
    }

    public static interface Packer {
        public static final int NOT_FOUND = -1;

        public int pack(MessageSignature var1);
    }

    public record Packed(int id, @Nullable MessageSignature fullSignature) {
        public static final int FULL_SIGNATURE = -1;

        public Packed(MessageSignature messageSignature) {
            this(-1, messageSignature);
        }

        public Packed(int i) {
            this(i, null);
        }

        public static Packed read(FriendlyByteBuf friendlyByteBuf) {
            int i = friendlyByteBuf.readVarInt() - 1;
            if (i == -1) {
                return new Packed(MessageSignature.read(friendlyByteBuf));
            }
            return new Packed(i);
        }

        public static void write(FriendlyByteBuf friendlyByteBuf, Packed packed) {
            friendlyByteBuf.writeVarInt(packed.id() + 1);
            if (packed.fullSignature() != null) {
                MessageSignature.write(friendlyByteBuf, packed.fullSignature());
            }
        }

        public Optional<MessageSignature> unpack(Unpacker unpacker) {
            if (this.fullSignature != null) {
                return Optional.of(this.fullSignature);
            }
            return Optional.ofNullable(unpacker.unpack(this.id));
        }

        @Nullable
        public MessageSignature fullSignature() {
            return this.fullSignature;
        }
    }

    public static interface Unpacker {
        @Nullable
        public MessageSignature unpack(int var1);
    }
}

