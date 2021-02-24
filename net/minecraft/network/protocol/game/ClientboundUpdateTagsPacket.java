/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagCollection;

public class ClientboundUpdateTagsPacket
implements Packet<ClientGamePacketListener> {
    private final Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> tags;

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map) {
        this.tags = map;
    }

    public ClientboundUpdateTagsPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.tags = friendlyByteBuf2.readMap(friendlyByteBuf -> ResourceKey.createRegistryKey(friendlyByteBuf.readResourceLocation()), TagCollection.NetworkPayload::read);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeMap(this.tags, (friendlyByteBuf, resourceKey) -> friendlyByteBuf.writeResourceLocation(resourceKey.location()), (friendlyByteBuf, networkPayload) -> networkPayload.write((FriendlyByteBuf)friendlyByteBuf));
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateTags(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> getTags() {
        return this.tags;
    }
}

