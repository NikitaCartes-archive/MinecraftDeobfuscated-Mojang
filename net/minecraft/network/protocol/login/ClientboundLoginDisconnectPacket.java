/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;

public class ClientboundLoginDisconnectPacket
implements Packet<ClientLoginPacketListener> {
    private final Component reason;

    public ClientboundLoginDisconnectPacket(Component component) {
        this.reason = component;
    }

    public ClientboundLoginDisconnectPacket(FriendlyByteBuf friendlyByteBuf) {
        this.reason = Component.Serializer.fromJsonLenient(friendlyByteBuf.readUtf(262144));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.reason);
    }

    @Override
    public void handle(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.handleDisconnect(this);
    }

    public Component getReason() {
        return this.reason;
    }
}

