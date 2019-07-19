package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
	private boolean reset;
	private Map<ResourceLocation, Advancement.Builder> added;
	private Set<ResourceLocation> removed;
	private Map<ResourceLocation, AdvancementProgress> progress;

	public ClientboundUpdateAdvancementsPacket() {
	}

	public ClientboundUpdateAdvancementsPacket(
		boolean bl, Collection<Advancement> collection, Set<ResourceLocation> set, Map<ResourceLocation, AdvancementProgress> map
	) {
		this.reset = bl;
		this.added = Maps.<ResourceLocation, Advancement.Builder>newHashMap();

		for (Advancement advancement : collection) {
			this.added.put(advancement.getId(), advancement.deconstruct());
		}

		this.removed = set;
		this.progress = Maps.<ResourceLocation, AdvancementProgress>newHashMap(map);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateAdvancementsPacket(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.reset = friendlyByteBuf.readBoolean();
		this.added = Maps.<ResourceLocation, Advancement.Builder>newHashMap();
		this.removed = Sets.<ResourceLocation>newLinkedHashSet();
		this.progress = Maps.<ResourceLocation, AdvancementProgress>newHashMap();
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			Advancement.Builder builder = Advancement.Builder.fromNetwork(friendlyByteBuf);
			this.added.put(resourceLocation, builder);
		}

		i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			this.removed.add(resourceLocation);
		}

		i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			this.progress.put(resourceLocation, AdvancementProgress.fromNetwork(friendlyByteBuf));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBoolean(this.reset);
		friendlyByteBuf.writeVarInt(this.added.size());

		for (Entry<ResourceLocation, Advancement.Builder> entry : this.added.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			Advancement.Builder builder = (Advancement.Builder)entry.getValue();
			friendlyByteBuf.writeResourceLocation(resourceLocation);
			builder.serializeToNetwork(friendlyByteBuf);
		}

		friendlyByteBuf.writeVarInt(this.removed.size());

		for (ResourceLocation resourceLocation2 : this.removed) {
			friendlyByteBuf.writeResourceLocation(resourceLocation2);
		}

		friendlyByteBuf.writeVarInt(this.progress.size());

		for (Entry<ResourceLocation, AdvancementProgress> entry : this.progress.entrySet()) {
			friendlyByteBuf.writeResourceLocation((ResourceLocation)entry.getKey());
			((AdvancementProgress)entry.getValue()).serializeToNetwork(friendlyByteBuf);
		}
	}

	@Environment(EnvType.CLIENT)
	public Map<ResourceLocation, Advancement.Builder> getAdded() {
		return this.added;
	}

	@Environment(EnvType.CLIENT)
	public Set<ResourceLocation> getRemoved() {
		return this.removed;
	}

	@Environment(EnvType.CLIENT)
	public Map<ResourceLocation, AdvancementProgress> getProgress() {
		return this.progress;
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldReset() {
		return this.reset;
	}
}
