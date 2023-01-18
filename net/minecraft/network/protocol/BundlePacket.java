/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

public abstract class BundlePacket<T extends PacketListener>
implements Packet<T> {
    private final Iterable<Packet<T>> packets;

    protected BundlePacket(Iterable<Packet<T>> iterable) {
        this.packets = iterable;
    }

    public final Iterable<Packet<T>> subPackets() {
        return this.packets;
    }

    @Override
    public final void write(FriendlyByteBuf friendlyByteBuf) {
    }
}

