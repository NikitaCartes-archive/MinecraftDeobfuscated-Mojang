/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundChatPacket
implements Packet<ClientGamePacketListener> {
    private Component message;
    private ChatType type;
    private UUID sender;

    public ClientboundChatPacket() {
    }

    public ClientboundChatPacket(Component component, ChatType chatType, UUID uUID) {
        this.message = component;
        this.type = chatType;
        this.sender = uUID;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.message = friendlyByteBuf.readComponent();
        this.type = ChatType.getForIndex(friendlyByteBuf.readByte());
        this.sender = friendlyByteBuf.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeComponent(this.message);
        friendlyByteBuf.writeByte(this.type.getIndex());
        friendlyByteBuf.writeUUID(this.sender);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleChat(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Component getMessage() {
        return this.message;
    }

    @Environment(value=EnvType.CLIENT)
    public ChatType getType() {
        return this.type;
    }

    @Environment(value=EnvType.CLIENT)
    public UUID getSender() {
        return this.sender;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

