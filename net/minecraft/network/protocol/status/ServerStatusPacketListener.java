/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;

public interface ServerStatusPacketListener
extends ServerPacketListener {
    public void handlePingRequest(ServerboundPingRequestPacket var1);

    public void handleStatusRequest(ServerboundStatusRequestPacket var1);
}

