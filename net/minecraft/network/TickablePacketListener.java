/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network;

import net.minecraft.network.PacketListener;

public interface TickablePacketListener
extends PacketListener {
    public void tick();
}

