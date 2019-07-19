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

public class ClientboundTabListPacket
implements Packet<ClientGamePacketListener> {
    private Component header;
    private Component footer;

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.header = friendlyByteBuf.readComponent();
        this.footer = friendlyByteBuf.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeComponent(this.header);
        friendlyByteBuf.writeComponent(this.footer);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTabListCustomisation(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Component getHeader() {
        return this.header;
    }

    @Environment(value=EnvType.CLIENT)
    public Component getFooter() {
        return this.footer;
    }
}

