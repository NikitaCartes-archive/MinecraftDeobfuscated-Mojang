/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ServerboundHelloPacket(String name, Optional<ProfilePublicKey> publicKey) implements Packet<ServerLoginPacketListener>
{
    public ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readUtf(16), friendlyByteBuf2.readOptional(friendlyByteBuf -> friendlyByteBuf.readWithCodec(ProfilePublicKey.CODEC)));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeUtf(this.name, 16);
        friendlyByteBuf2.writeOptional(this.publicKey, (friendlyByteBuf, profilePublicKey) -> friendlyByteBuf.writeWithCodec(ProfilePublicKey.CODEC, profilePublicKey));
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleHello(this);
    }
}

