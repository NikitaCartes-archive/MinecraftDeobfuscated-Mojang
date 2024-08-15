package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.redstone.Orientation;

public record RedstoneWireOrientationsDebugPayload(long time, List<RedstoneWireOrientationsDebugPayload.Wire> wires) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RedstoneWireOrientationsDebugPayload> TYPE = CustomPacketPayload.createType("debug/redstone_update_order");
	public static final StreamCodec<FriendlyByteBuf, RedstoneWireOrientationsDebugPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG,
		RedstoneWireOrientationsDebugPayload::time,
		RedstoneWireOrientationsDebugPayload.Wire.STREAM_CODEC.apply(ByteBufCodecs.list()),
		RedstoneWireOrientationsDebugPayload::wires,
		RedstoneWireOrientationsDebugPayload::new
	);

	@Override
	public CustomPacketPayload.Type<RedstoneWireOrientationsDebugPayload> type() {
		return TYPE;
	}

	public static record Wire(BlockPos pos, Orientation orientation) {
		public static final StreamCodec<ByteBuf, RedstoneWireOrientationsDebugPayload.Wire> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC,
			RedstoneWireOrientationsDebugPayload.Wire::pos,
			Orientation.STREAM_CODEC,
			RedstoneWireOrientationsDebugPayload.Wire::orientation,
			RedstoneWireOrientationsDebugPayload.Wire::new
		);
	}
}
