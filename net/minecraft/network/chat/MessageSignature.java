/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Crypt;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(UUID sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature) {
    public static MessageSignature unsigned(UUID uUID) {
        return new MessageSignature(uUID, Instant.now(), Crypt.SaltSignaturePair.EMPTY);
    }

    public boolean verify(SignatureValidator signatureValidator, Component component) {
        if (this.isValid()) {
            return signatureValidator.validate(output -> MessageSignature.updateSignature(output, component, this.sender, this.timeStamp, this.saltSignature.salt()), this.saltSignature.signature());
        }
        return false;
    }

    public boolean verify(SignatureValidator signatureValidator, String string) throws SignatureException {
        return this.verify(signatureValidator, Component.literal(string));
    }

    public static void updateSignature(SignatureUpdater.Output output, Component component, UUID uUID, Instant instant, long l) throws SignatureException {
        byte[] bs = new byte[32];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putLong(l);
        byteBuffer.putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
        byteBuffer.putLong(instant.getEpochSecond());
        output.update(bs);
        output.update(MessageSignature.encodeContent(component));
    }

    private static byte[] encodeContent(Component component) {
        String string = Component.Serializer.toStableJson(component);
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValid() {
        return this.sender != Util.NIL_UUID && this.saltSignature.isValid();
    }
}

