/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

public interface ServerPacketListener
extends PacketListener {
    @Override
    default public boolean shouldPropagateHandlingExceptions() {
        return false;
    }
}

