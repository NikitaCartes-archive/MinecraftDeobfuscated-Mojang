/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, MessageSignature signature, boolean signedPreview) implements Packet<ServerGamePacketListener>
{
    public ServerboundChatPacket(String string, Instant instant, long l, MessageSignature messageSignature, boolean bl) {
        this.message = string = StringUtil.trimChatMessage(string);
        this.timeStamp = instant;
        this.salt = l;
        this.signature = messageSignature;
        this.signedPreview = bl;
    }

    public ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(256), friendlyByteBuf.readInstant(), friendlyByteBuf.readLong(), new MessageSignature(friendlyByteBuf), friendlyByteBuf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.message, 256);
        friendlyByteBuf.writeInstant(this.timeStamp);
        friendlyByteBuf.writeLong(this.salt);
        this.signature.write(friendlyByteBuf);
        friendlyByteBuf.writeBoolean(this.signedPreview);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleChat(this);
    }

    public MessageSigner getSigner(ServerPlayer serverPlayer) {
        return new MessageSigner(serverPlayer.getUUID(), this.timeStamp, this.salt);
    }
}

