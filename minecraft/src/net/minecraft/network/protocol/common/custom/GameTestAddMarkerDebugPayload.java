package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record GameTestAddMarkerDebugPayload(BlockPos pos, int color, String text, int durationMs) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/game_test_add_marker");

	public GameTestAddMarkerDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readInt(), friendlyByteBuf.readUtf(), friendlyByteBuf.readInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeInt(this.color);
		friendlyByteBuf.writeUtf(this.text);
		friendlyByteBuf.writeInt(this.durationMs);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
