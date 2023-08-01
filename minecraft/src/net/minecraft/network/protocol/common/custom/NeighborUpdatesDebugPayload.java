package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record NeighborUpdatesDebugPayload(long time, BlockPos pos) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/neighbors_update");

	public NeighborUpdatesDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarLong(), friendlyByteBuf.readBlockPos());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarLong(this.time);
		friendlyByteBuf.writeBlockPos(this.pos);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
