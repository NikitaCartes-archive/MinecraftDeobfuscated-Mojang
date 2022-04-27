/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;

public class ClientboundGameProfilePacket
implements Packet<ClientLoginPacketListener> {
    private final GameProfile gameProfile;

    public ClientboundGameProfilePacket(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public ClientboundGameProfilePacket(FriendlyByteBuf friendlyByteBuf) {
        this.gameProfile = friendlyByteBuf.readGameProfile();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeGameProfile(this.gameProfile);
    }

    @Override
    public void handle(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.handleGameProfile(this);
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}

