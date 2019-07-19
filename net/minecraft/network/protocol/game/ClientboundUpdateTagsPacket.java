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
import net.minecraft.tags.TagManager;

public class ClientboundUpdateTagsPacket
implements Packet<ClientGamePacketListener> {
    private TagManager tags;

    public ClientboundUpdateTagsPacket() {
    }

    public ClientboundUpdateTagsPacket(TagManager tagManager) {
        this.tags = tagManager;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.tags = TagManager.deserializeFromNetwork(friendlyByteBuf);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.tags.serializeToNetwork(friendlyByteBuf);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateTags(this);
    }

    @Environment(value=EnvType.CLIENT)
    public TagManager getTags() {
        return this.tags;
    }
}

