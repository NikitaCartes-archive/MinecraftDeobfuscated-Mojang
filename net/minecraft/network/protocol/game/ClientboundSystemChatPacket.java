/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ClientboundSystemChatPacket(Component content, int typeId) implements Packet<ClientGamePacketListener>
{
    public ClientboundSystemChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readComponent(), friendlyByteBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.content);
        friendlyByteBuf.writeVarInt(this.typeId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSystemChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public ChatType resolveType(Registry<ChatType> registry) {
        return Objects.requireNonNull((ChatType)registry.byId(this.typeId), "Invalid chat type");
    }
}

