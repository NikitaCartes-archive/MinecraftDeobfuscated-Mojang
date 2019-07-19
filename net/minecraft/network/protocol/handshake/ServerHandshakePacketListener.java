/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.handshake;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;

public interface ServerHandshakePacketListener
extends PacketListener {
    public void handleIntention(ClientIntentionPacket var1);
}

