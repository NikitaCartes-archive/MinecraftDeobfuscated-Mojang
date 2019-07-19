/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundDisconnectPacket
implements Packet<ClientGamePacketListener> {
    private Component reason;

    public ClientboundDisconnectPacket() {
    }

    public ClientboundDisconnectPacket(Component component) {
        this.reason = component;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.reason = friendlyByteBuf.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeComponent(this.reason);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleDisconnect(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Component getReason() {
        return this.reason;
    }
}

