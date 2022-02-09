package net.minecraft.network.protocol.game;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
	private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags;

	public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> map) {
		this.tags = map;
	}

	public ClientboundUpdateTagsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.tags = friendlyByteBuf.readMap(
			friendlyByteBufx -> ResourceKey.createRegistryKey(friendlyByteBufx.readResourceLocation()), TagNetworkSerialization.NetworkPayload::read
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeMap(
			this.tags,
			(friendlyByteBufx, resourceKey) -> friendlyByteBufx.writeResourceLocation(resourceKey.location()),
			(friendlyByteBufx, networkPayload) -> networkPayload.write(friendlyByteBufx)
		);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateTags(this);
	}

	public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> getTags() {
		return this.tags;
	}
}
