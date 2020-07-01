package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.TagContainer;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
	private TagContainer tags;

	public ClientboundUpdateTagsPacket() {
	}

	public ClientboundUpdateTagsPacket(TagContainer tagContainer) {
		this.tags = tagContainer;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.tags = TagContainer.deserializeFromNetwork(friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.tags.serializeToNetwork(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateTags(this);
	}

	@Environment(EnvType.CLIENT)
	public TagContainer getTags() {
		return this.tags;
	}
}
