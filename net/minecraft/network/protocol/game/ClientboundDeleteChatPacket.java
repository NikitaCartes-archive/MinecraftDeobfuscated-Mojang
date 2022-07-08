/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ClientboundDeleteChatPacket(MessageSignature messageSignature) implements Packet<ClientGamePacketListener>
{
    public ClientboundDeleteChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(new MessageSignature(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        this.messageSignature.write(friendlyByteBuf);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleDeleteChat(this);
    }
}

