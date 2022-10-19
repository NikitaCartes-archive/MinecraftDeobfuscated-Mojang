/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.jetbrains.annotations.Nullable;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener>
{
    public ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(256), friendlyByteBuf.readInstant(), friendlyByteBuf.readLong(), (MessageSignature)friendlyByteBuf.readNullable(MessageSignature::read), new LastSeenMessages.Update(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.message, 256);
        friendlyByteBuf.writeInstant(this.timeStamp);
        friendlyByteBuf.writeLong(this.salt);
        friendlyByteBuf.writeNullable(this.signature, MessageSignature::write);
        this.lastSeenMessages.write(friendlyByteBuf);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleChat(this);
    }

    @Nullable
    public MessageSignature signature() {
        return this.signature;
    }
}

