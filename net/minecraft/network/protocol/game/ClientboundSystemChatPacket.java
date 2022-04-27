/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ClientboundSystemChatPacket(Component content, ChatType type) implements Packet<ClientGamePacketListener>
{
    public ClientboundSystemChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readComponent(), ChatType.getForIndex(friendlyByteBuf.readByte()));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.content);
        friendlyByteBuf.writeByte(this.type.getIndex());
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSystemChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

