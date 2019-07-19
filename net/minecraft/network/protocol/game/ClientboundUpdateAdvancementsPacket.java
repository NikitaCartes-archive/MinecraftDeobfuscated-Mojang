/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket
implements Packet<ClientGamePacketListener> {
    private boolean reset;
    private Map<ResourceLocation, Advancement.Builder> added;
    private Set<ResourceLocation> removed;
    private Map<ResourceLocation, AdvancementProgress> progress;

    public ClientboundUpdateAdvancementsPacket() {
    }

    public ClientboundUpdateAdvancementsPacket(boolean bl, Collection<Advancement> collection, Set<ResourceLocation> set, Map<ResourceLocation, AdvancementProgress> map) {
        this.reset = bl;
        this.added = Maps.newHashMap();
        for (Advancement advancement : collection) {
            this.added.put(advancement.getId(), advancement.deconstruct());
        }
        this.removed = set;
        this.progress = Maps.newHashMap(map);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateAdvancementsPacket(this);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        ResourceLocation resourceLocation;
        int j;
        this.reset = friendlyByteBuf.readBoolean();
        this.added = Maps.newHashMap();
        this.removed = Sets.newLinkedHashSet();
        this.progress = Maps.newHashMap();
        int i = friendlyByteBuf.readVarInt();
        for (j = 0; j < i; ++j) {
            resourceLocation = friendlyByteBuf.readResourceLocation();
            Advancement.Builder builder = Advancement.Builder.fromNetwork(friendlyByteBuf);
            this.added.put(resourceLocation, builder);
        }
        i = friendlyByteBuf.readVarInt();
        for (j = 0; j < i; ++j) {
            resourceLocation = friendlyByteBuf.readResourceLocation();
            this.removed.add(resourceLocation);
        }
        i = friendlyByteBuf.readVarInt();
        for (j = 0; j < i; ++j) {
            resourceLocation = friendlyByteBuf.readResourceLocation();
            this.progress.put(resourceLocation, AdvancementProgress.fromNetwork(friendlyByteBuf));
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeBoolean(this.reset);
        friendlyByteBuf.writeVarInt(this.added.size());
        for (Map.Entry<ResourceLocation, Advancement.Builder> entry : this.added.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            Advancement.Builder builder = entry.getValue();
            friendlyByteBuf.writeResourceLocation(resourceLocation);
            builder.serializeToNetwork(friendlyByteBuf);
        }
        friendlyByteBuf.writeVarInt(this.removed.size());
        for (ResourceLocation resourceLocation : this.removed) {
            friendlyByteBuf.writeResourceLocation(resourceLocation);
        }
        friendlyByteBuf.writeVarInt(this.progress.size());
        for (Map.Entry entry : this.progress.entrySet()) {
            friendlyByteBuf.writeResourceLocation((ResourceLocation)entry.getKey());
            ((AdvancementProgress)entry.getValue()).serializeToNetwork(friendlyByteBuf);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public Map<ResourceLocation, Advancement.Builder> getAdded() {
        return this.added;
    }

    @Environment(value=EnvType.CLIENT)
    public Set<ResourceLocation> getRemoved() {
        return this.removed;
    }

    @Environment(value=EnvType.CLIENT)
    public Map<ResourceLocation, AdvancementProgress> getProgress() {
        return this.progress;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldReset() {
        return this.reset;
    }
}

