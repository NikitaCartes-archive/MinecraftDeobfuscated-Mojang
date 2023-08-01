package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record WorldGenAttemptDebugPayload(BlockPos pos, float scale, float red, float green, float blue, float alpha) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/worldgen_attempt");

	public WorldGenAttemptDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readBlockPos(),
			friendlyByteBuf.readFloat(),
			friendlyByteBuf.readFloat(),
			friendlyByteBuf.readFloat(),
			friendlyByteBuf.readFloat(),
			friendlyByteBuf.readFloat()
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeFloat(this.scale);
		friendlyByteBuf.writeFloat(this.red);
		friendlyByteBuf.writeFloat(this.green);
		friendlyByteBuf.writeFloat(this.blue);
		friendlyByteBuf.writeFloat(this.alpha);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
