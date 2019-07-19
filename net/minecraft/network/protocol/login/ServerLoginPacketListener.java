/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;

public interface ServerLoginPacketListener
extends PacketListener {
    public void handleHello(ServerboundHelloPacket var1);

    public void handleKey(ServerboundKeyPacket var1);

    public void handleCustomQueryPacket(ServerboundCustomQueryPacket var1);
}

