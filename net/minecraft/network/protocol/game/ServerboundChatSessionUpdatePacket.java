/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener>
{
    public ServerboundChatSessionUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
        this(RemoteChatSession.Data.read(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        RemoteChatSession.Data.write(friendlyByteBuf, this.chatSession);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleChatSessionUpdate(this);
    }
}

