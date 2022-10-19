/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public record ClientboundPlayerChatPacket(UUID sender, int index, @Nullable MessageSignature signature, SignedMessageBody.Packed body, @Nullable Component unsignedContent, FilterMask filterMask, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener>
{
    public ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUUID(), friendlyByteBuf.readVarInt(), (MessageSignature)friendlyByteBuf.readNullable(MessageSignature::read), new SignedMessageBody.Packed(friendlyByteBuf), (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent), FilterMask.read(friendlyByteBuf), new ChatType.BoundNetwork(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.sender);
        friendlyByteBuf.writeVarInt(this.index);
        friendlyByteBuf.writeNullable(this.signature, MessageSignature::write);
        this.body.write(friendlyByteBuf);
        friendlyByteBuf.writeNullable(this.unsignedContent, FriendlyByteBuf::writeComponent);
        FilterMask.write(friendlyByteBuf, this.filterMask);
        this.chatType.write(friendlyByteBuf);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    @Nullable
    public MessageSignature signature() {
        return this.signature;
    }

    @Nullable
    public Component unsignedContent() {
        return this.unsignedContent;
    }
}

