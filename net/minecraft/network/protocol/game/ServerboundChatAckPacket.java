/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundChatAckPacket(LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener>
{
    public ServerboundChatAckPacket(FriendlyByteBuf friendlyByteBuf) {
        this(new LastSeenMessages.Update(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        this.lastSeenMessages.write(friendlyByteBuf);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleChatAck(this);
    }
}

