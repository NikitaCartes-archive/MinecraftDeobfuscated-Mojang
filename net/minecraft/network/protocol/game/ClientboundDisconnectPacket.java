/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundDisconnectPacket
implements Packet<ClientGamePacketListener> {
    private final Component reason;

    public ClientboundDisconnectPacket(Component component) {
        this.reason = component;
    }

    public ClientboundDisconnectPacket(FriendlyByteBuf friendlyByteBuf) {
        this.reason = friendlyByteBuf.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.reason);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleDisconnect(this);
    }

    public Component getReason() {
        return this.reason;
    }
}

