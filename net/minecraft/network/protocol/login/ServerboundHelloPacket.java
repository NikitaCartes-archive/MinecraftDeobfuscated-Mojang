/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;

public class ServerboundHelloPacket
implements Packet<ServerLoginPacketListener> {
    private final GameProfile gameProfile;

    public ServerboundHelloPacket(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
        this.gameProfile = new GameProfile(null, friendlyByteBuf.readUtf(16));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.gameProfile.getName());
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleHello(this);
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}

