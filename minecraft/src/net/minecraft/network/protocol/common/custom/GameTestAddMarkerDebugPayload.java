package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record GameTestAddMarkerDebugPayload(BlockPos pos, int color, String text, int durationMs) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, GameTestAddMarkerDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
		GameTestAddMarkerDebugPayload::write, GameTestAddMarkerDebugPayload::new
	);
	public static final CustomPacketPayload.Type<GameTestAddMarkerDebugPayload> TYPE = CustomPacketPayload.createType("debug/game_test_add_marker");

	private GameTestAddMarkerDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readInt(), friendlyByteBuf.readUtf(), friendlyByteBuf.readInt());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeInt(this.color);
		friendlyByteBuf.writeUtf(this.text);
		friendlyByteBuf.writeInt(this.durationMs);
	}

	@Override
	public CustomPacketPayload.Type<GameTestAddMarkerDebugPayload> type() {
		return TYPE;
	}
}
