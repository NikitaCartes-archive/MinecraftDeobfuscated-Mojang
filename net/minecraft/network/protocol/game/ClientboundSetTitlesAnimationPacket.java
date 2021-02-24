/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetTitlesAnimationPacket
implements Packet<ClientGamePacketListener> {
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public ClientboundSetTitlesAnimationPacket(int i, int j, int k) {
        this.fadeIn = i;
        this.stay = j;
        this.fadeOut = k;
    }

    public ClientboundSetTitlesAnimationPacket(FriendlyByteBuf friendlyByteBuf) {
        this.fadeIn = friendlyByteBuf.readInt();
        this.stay = friendlyByteBuf.readInt();
        this.fadeOut = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.fadeIn);
        friendlyByteBuf.writeInt(this.stay);
        friendlyByteBuf.writeInt(this.fadeOut);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.setTitlesAnimation(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getFadeIn() {
        return this.fadeIn;
    }

    @Environment(value=EnvType.CLIENT)
    public int getStay() {
        return this.stay;
    }

    @Environment(value=EnvType.CLIENT)
    public int getFadeOut() {
        return this.fadeOut;
    }
}

