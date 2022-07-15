/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.util.SignatureValidator;
import org.jetbrains.annotations.Nullable;

public record MessageSignature(byte[] bytes) {
    public static final MessageSignature EMPTY = new MessageSignature(ByteArrays.EMPTY_ARRAY);

    public MessageSignature(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readByteArray());
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByteArray(this.bytes);
    }

    public boolean verify(SignatureValidator signatureValidator, SignedMessageHeader signedMessageHeader, SignedMessageBody signedMessageBody) {
        if (!this.isEmpty()) {
            byte[] bs = signedMessageBody.hash().asBytes();
            return signatureValidator.validate(output -> signedMessageHeader.updateSignature(output, bs), this.bytes);
        }
        return false;
    }

    public boolean verify(SignatureValidator signatureValidator, SignedMessageHeader signedMessageHeader, byte[] bs) {
        if (!this.isEmpty()) {
            return signatureValidator.validate(output -> signedMessageHeader.updateSignature(output, bs), this.bytes);
        }
        return false;
    }

    public boolean isEmpty() {
        return this.bytes.length == 0;
    }

    @Nullable
    public ByteBuffer asByteBuffer() {
        if (!this.isEmpty()) {
            return ByteBuffer.wrap(this.bytes);
        }
        return null;
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
        if (!this.isEmpty()) {
            return Base64.getEncoder().encodeToString(this.bytes);
        }
        return "empty";
    }
}

