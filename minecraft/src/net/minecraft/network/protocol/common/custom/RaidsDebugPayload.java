package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record RaidsDebugPayload(List<BlockPos> raidCenters) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/raids");

	public RaidsDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readList(FriendlyByteBuf::readBlockPos));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.raidCenters, FriendlyByteBuf::writeBlockPos);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
