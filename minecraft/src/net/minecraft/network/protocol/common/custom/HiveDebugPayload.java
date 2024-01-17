package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record HiveDebugPayload(HiveDebugPayload.HiveInfo hiveInfo) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, HiveDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(HiveDebugPayload::write, HiveDebugPayload::new);
	public static final CustomPacketPayload.Type<HiveDebugPayload> TYPE = CustomPacketPayload.createType("debug/hive");

	private HiveDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(new HiveDebugPayload.HiveInfo(friendlyByteBuf));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		this.hiveInfo.write(friendlyByteBuf);
	}

	@Override
	public CustomPacketPayload.Type<HiveDebugPayload> type() {
		return TYPE;
	}

	public static record HiveInfo(BlockPos pos, String hiveType, int occupantCount, int honeyLevel, boolean sedated) {
		public HiveInfo(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readUtf(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readBoolean());
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeBlockPos(this.pos);
			friendlyByteBuf.writeUtf(this.hiveType);
			friendlyByteBuf.writeInt(this.occupantCount);
			friendlyByteBuf.writeInt(this.honeyLevel);
			friendlyByteBuf.writeBoolean(this.sedated);
		}
	}
}
