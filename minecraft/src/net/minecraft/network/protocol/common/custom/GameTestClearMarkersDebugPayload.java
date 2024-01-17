package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record GameTestClearMarkersDebugPayload() implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, GameTestClearMarkersDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
		GameTestClearMarkersDebugPayload::write, GameTestClearMarkersDebugPayload::new
	);
	public static final CustomPacketPayload.Type<GameTestClearMarkersDebugPayload> TYPE = CustomPacketPayload.createType("debug/game_test_clear");

	private GameTestClearMarkersDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public CustomPacketPayload.Type<GameTestClearMarkersDebugPayload> type() {
		return TYPE;
	}
}
