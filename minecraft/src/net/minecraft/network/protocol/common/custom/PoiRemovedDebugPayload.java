package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PoiRemovedDebugPayload(BlockPos pos) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/poi_removed");

	public PoiRemovedDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readBlockPos());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
