/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;

public class ServerboundHelloPacket
implements Packet<ServerLoginPacketListener> {
    private GameProfile gameProfile;

    public ServerboundHelloPacket() {
    }

    public ServerboundHelloPacket(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.gameProfile = new GameProfile(null, friendlyByteBuf.readUtf(16));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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

