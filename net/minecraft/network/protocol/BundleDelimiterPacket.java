/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

public class BundleDelimiterPacket<T extends PacketListener>
implements Packet<T> {
    @Override
    public final void write(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public final void handle(T packetListener) {
        throw new AssertionError((Object)"This packet should be handled by pipeline");
    }
}

