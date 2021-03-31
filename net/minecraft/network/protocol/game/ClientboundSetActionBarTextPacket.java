/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetActionBarTextPacket
implements Packet<ClientGamePacketListener> {
    private final Component text;

    public ClientboundSetActionBarTextPacket(Component component) {
        this.text = component;
    }

    public ClientboundSetActionBarTextPacket(FriendlyByteBuf friendlyByteBuf) {
        this.text = friendlyByteBuf.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.text);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.setActionBarText(this);
    }

    public Component getText() {
        return this.text;
    }
}

