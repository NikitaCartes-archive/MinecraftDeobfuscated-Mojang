/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;

public class ClientboundUpdateTagsPacket
implements Packet<ClientGamePacketListener> {
    private Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> tags;

    public ClientboundUpdateTagsPacket() {
    }

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map) {
        this.tags = map;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        int i = friendlyByteBuf.readVarInt();
        this.tags = Maps.newHashMapWithExpectedSize(i);
        for (int j = 0; j < i; ++j) {
            ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
            ResourceKey resourceKey = ResourceKey.createRegistryKey(resourceLocation);
            TagCollection.NetworkPayload networkPayload = TagCollection.NetworkPayload.read(friendlyByteBuf);
            this.tags.put(resourceKey, networkPayload);
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.tags.size());
        this.tags.forEach((resourceKey, networkPayload) -> {
            friendlyByteBuf.writeResourceLocation(resourceKey.location());
            networkPayload.write(friendlyByteBuf);
        });
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

