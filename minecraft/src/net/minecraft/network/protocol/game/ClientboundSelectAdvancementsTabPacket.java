package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
	@Nullable
	private ResourceLocation tab;

	public ClientboundSelectAdvancementsTabPacket() {
	}

	public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation resourceLocation) {
		this.tab = resourceLocation;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSelectAdvancementsTab(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		if (friendlyByteBuf.readBoolean()) {
			this.tab = friendlyByteBuf.readResourceLocation();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBoolean(this.tab != null);
		if (this.tab != null) {
			friendlyByteBuf.writeResourceLocation(this.tab);
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public ResourceLocation getTab() {
		return this.tab;
	}
}
