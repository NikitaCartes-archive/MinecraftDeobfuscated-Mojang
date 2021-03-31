package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
	@Nullable
	private final ResourceLocation tab;

	public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation resourceLocation) {
		this.tab = resourceLocation;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSelectAdvancementsTab(this);
	}

	public ClientboundSelectAdvancementsTabPacket(FriendlyByteBuf friendlyByteBuf) {
		if (friendlyByteBuf.readBoolean()) {
			this.tab = friendlyByteBuf.readResourceLocation();
		} else {
			this.tab = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.tab != null);
		if (this.tab != null) {
			friendlyByteBuf.writeResourceLocation(this.tab);
		}
	}

	@Nullable
	public ResourceLocation getTab() {
		return this.tab;
	}
}
