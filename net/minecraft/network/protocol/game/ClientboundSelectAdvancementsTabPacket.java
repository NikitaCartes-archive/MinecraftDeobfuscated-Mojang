/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ClientboundSelectAdvancementsTabPacket
implements Packet<ClientGamePacketListener> {
    @Nullable
    private ResourceLocation tab;

    public ClientboundSelectAdvancementsTabPacket() {
    }

    public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation resourceLocation) {
        this.tab = resourceLocation;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSelectAdvancementsTab(this);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        if (friendlyByteBuf.readBoolean()) {
            this.tab = friendlyByteBuf.readResourceLocation();
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeBoolean(this.tab != null);
        if (this.tab != null) {
            friendlyByteBuf.writeResourceLocation(this.tab);
        }
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public ResourceLocation getTab() {
        return this.tab;
    }
}

