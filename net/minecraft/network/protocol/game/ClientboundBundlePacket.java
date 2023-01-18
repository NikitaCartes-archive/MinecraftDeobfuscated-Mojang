/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundBundlePacket
extends BundlePacket<ClientGamePacketListener> {
    public ClientboundBundlePacket(Iterable<Packet<ClientGamePacketListener>> iterable) {
        super(iterable);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBundlePacket(this);
    }
}

