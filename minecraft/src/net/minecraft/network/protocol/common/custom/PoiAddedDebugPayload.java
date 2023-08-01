package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PoiAddedDebugPayload(BlockPos pos, String type, int freeTicketCount) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/poi_added");

	public PoiAddedDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readUtf(), friendlyByteBuf.readInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeUtf(this.type);
		friendlyByteBuf.writeInt(this.freeTicketCount);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
