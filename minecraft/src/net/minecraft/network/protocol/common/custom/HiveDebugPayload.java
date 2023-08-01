package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record HiveDebugPayload(HiveDebugPayload.HiveInfo hiveInfo) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/hive");

	public HiveDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(new HiveDebugPayload.HiveInfo(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.hiveInfo.write(friendlyByteBuf);
	}

	@Override
	public ResourceLocation id() {
		return ID;
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
