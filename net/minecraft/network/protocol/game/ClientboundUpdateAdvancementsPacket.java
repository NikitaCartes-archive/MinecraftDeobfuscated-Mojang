/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
    private final boolean reset;
    private final Map<ResourceLocation, Advancement.Builder> added;
    private final Set<ResourceLocation> removed;
    private final Map<ResourceLocation, AdvancementProgress> progress;

    public ClientboundUpdateAdvancementsPacket(boolean bl, Collection<Advancement> collection, Set<ResourceLocation> set, Map<ResourceLocation, AdvancementProgress> map) {
        this.reset = bl;
        ImmutableMap.Builder<ResourceLocation, Advancement.Builder> builder = ImmutableMap.builder();
        for (Advancement advancement : collection) {
            builder.put(advancement.getId(), advancement.deconstruct());
        }
        this.added = builder.build();
        this.removed = ImmutableSet.copyOf(set);
        this.progress = ImmutableMap.copyOf(map);
    }

    public ClientboundUpdateAdvancementsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.reset = friendlyByteBuf.readBoolean();
        this.added = friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, Advancement.Builder::fromNetwork);
        this.removed = friendlyByteBuf.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
        this.progress = friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeBoolean(this.reset);
        friendlyByteBuf2.writeMap(this.added, FriendlyByteBuf::writeResourceLocation, (friendlyByteBuf, builder) -> builder.serializeToNetwork((FriendlyByteBuf)friendlyByteBuf));
        friendlyByteBuf2.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
        friendlyByteBuf2.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (friendlyByteBuf, advancementProgress) -> advancementProgress.serializeToNetwork((FriendlyByteBuf)friendlyByteBuf));
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateAdvancementsPacket(this);
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

