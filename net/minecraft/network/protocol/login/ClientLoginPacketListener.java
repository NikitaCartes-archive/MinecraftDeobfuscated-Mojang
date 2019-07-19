/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;

public interface ClientLoginPacketListener
extends PacketListener {
    public void handleHello(ClientboundHelloPacket var1);

    public void handleGameProfile(ClientboundGameProfilePacket var1);

    public void handleDisconnect(ClientboundLoginDisconnectPacket var1);

    public void handleCompression(ClientboundLoginCompressionPacket var1);

    public void handleCustomQuery(ClientboundCustomQueryPacket var1);
}

