/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetTitleTextPacket
implements Packet<ClientGamePacketListener> {
    private final Component text;

    public ClientboundSetTitleTextPacket(Component component) {
        this.text = component;
    }

    public ClientboundSetTitleTextPacket(FriendlyByteBuf friendlyByteBuf) {
        this.text = friendlyByteBuf.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.text);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.setTitleText(this);
    }

    public Component getText() {
        return this.text;
    }
}

