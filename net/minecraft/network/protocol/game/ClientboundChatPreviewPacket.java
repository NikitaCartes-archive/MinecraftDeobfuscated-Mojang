/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public record ClientboundChatPreviewPacket(int queryId, @Nullable Component preview) implements Packet<ClientGamePacketListener>
{
    public ClientboundChatPreviewPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readInt(), (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.queryId);
        friendlyByteBuf.writeNullable(this.preview, FriendlyByteBuf::writeComponent);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleChatPreview(this);
    }

    @Nullable
    public Component preview() {
        return this.preview;
    }
}

