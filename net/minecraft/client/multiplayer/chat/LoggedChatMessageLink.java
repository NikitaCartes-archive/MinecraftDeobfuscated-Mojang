/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;

@Environment(value=EnvType.CLIENT)
public interface LoggedChatMessageLink
extends LoggedChatEvent {
    public static Header header(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
        return new Header(signedMessageHeader, messageSignature, bs);
    }

    public SignedMessageHeader header();

    public MessageSignature headerSignature();

    public byte[] bodyDigest();

    @Environment(value=EnvType.CLIENT)
    public record Header(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest) implements LoggedChatMessageLink
    {
    }
}

