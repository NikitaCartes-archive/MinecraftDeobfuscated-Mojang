/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundResourcePackPacket
implements Packet<ClientGamePacketListener> {
    private final String url;
    private final String hash;
    private final boolean required;

    public ClientboundResourcePackPacket(String string, String string2, boolean bl) {
        if (string2.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + string2.length() + ")");
        }
        this.url = string;
        this.hash = string2;
        this.required = bl;
    }

    public ClientboundResourcePackPacket(FriendlyByteBuf friendlyByteBuf) {
        this.url = friendlyByteBuf.readUtf();
        this.hash = friendlyByteBuf.readUtf(40);
        this.required = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.url);
        friendlyByteBuf.writeUtf(this.hash);
        friendlyByteBuf.writeBoolean(this.required);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleResourcePack(this);
    }

    @Environment(value=EnvType.CLIENT)
    public String getUrl() {
        return this.url;
    }

    @Environment(value=EnvType.CLIENT)
    public String getHash() {
        return this.hash;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isRequired() {
        return this.required;
    }
}

