/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.security.SignatureException;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.SignatureUpdater;
import org.jetbrains.annotations.Nullable;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature, UUID sender) {
    public SignedMessageHeader(FriendlyByteBuf friendlyByteBuf) {
        this((MessageSignature)friendlyByteBuf.readNullable(MessageSignature::new), friendlyByteBuf.readUUID());
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeNullable(this.previousSignature, (friendlyByteBuf, messageSignature) -> messageSignature.write((FriendlyByteBuf)friendlyByteBuf));
        friendlyByteBuf2.writeUUID(this.sender);
    }

    public void updateSignature(SignatureUpdater.Output output, byte[] bs) throws SignatureException {
        if (this.previousSignature != null) {
            output.update(this.previousSignature.bytes());
        }
        output.update(UUIDUtil.uuidToByteArray(this.sender));
        output.update(bs);
    }

    @Nullable
    public MessageSignature previousSignature() {
        return this.previousSignature;
    }
}

