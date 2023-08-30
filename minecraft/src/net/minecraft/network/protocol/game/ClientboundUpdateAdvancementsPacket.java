package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
	private final boolean reset;
	private final List<AdvancementHolder> added;
	private final Set<ResourceLocation> removed;
	private final Map<ResourceLocation, AdvancementProgress> progress;

	public ClientboundUpdateAdvancementsPacket(
		boolean bl, Collection<AdvancementHolder> collection, Set<ResourceLocation> set, Map<ResourceLocation, AdvancementProgress> map
	) {
		this.reset = bl;
		this.added = List.copyOf(collection);
		this.removed = Set.copyOf(set);
		this.progress = Map.copyOf(map);
	}

	public ClientboundUpdateAdvancementsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.reset = friendlyByteBuf.readBoolean();
		this.added = friendlyByteBuf.readList(AdvancementHolder::read);
		this.removed = friendlyByteBuf.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
		this.progress = friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.reset);
		friendlyByteBuf.writeCollection(this.added, (friendlyByteBufx, advancementHolder) -> advancementHolder.write(friendlyByteBufx));
		friendlyByteBuf.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
		friendlyByteBuf.writeMap(
			this.progress, FriendlyByteBuf::writeResourceLocation, (friendlyByteBufx, advancementProgress) -> advancementProgress.serializeToNetwork(friendlyByteBufx)
		);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateAdvancementsPacket(this);
	}

	public List<AdvancementHolder> getAdded() {
		return this.added;
	}

	public Set<ResourceLocation> getRemoved() {
		return this.removed;
	}

	public Map<ResourceLocation, AdvancementProgress> getProgress() {
		return this.progress;
	}

	public boolean shouldReset() {
		return this.reset;
	}
}
