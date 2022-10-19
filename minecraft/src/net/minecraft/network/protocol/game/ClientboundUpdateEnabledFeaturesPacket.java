package net.minecraft.network.protocol.game;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateEnabledFeaturesPacket(Set<ResourceLocation> features) implements Packet<ClientGamePacketListener> {
	public ClientboundUpdateEnabledFeaturesPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.features, FriendlyByteBuf::writeResourceLocation);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleEnabledFeatures(this);
	}
}
