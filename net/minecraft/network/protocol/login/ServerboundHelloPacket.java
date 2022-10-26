/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;

public record ServerboundHelloPacket(String name, Optional<UUID> profileId) implements Packet<ServerLoginPacketListener>
{
    public ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(16), friendlyByteBuf.readOptional(FriendlyByteBuf::readUUID));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.name, 16);
        friendlyByteBuf.writeOptional(this.profileId, FriendlyByteBuf::writeUUID);
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleHello(this);
    }
}

