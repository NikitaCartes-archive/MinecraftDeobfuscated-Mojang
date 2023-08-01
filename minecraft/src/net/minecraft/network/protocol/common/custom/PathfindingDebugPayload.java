package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.pathfinder.Path;

public record PathfindingDebugPayload(int entityId, Path path, float maxNodeDistance) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/path");

	public PathfindingDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readInt(), Path.createFromStream(friendlyByteBuf), friendlyByteBuf.readFloat());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.entityId);
		this.path.writeToStream(friendlyByteBuf);
		friendlyByteBuf.writeFloat(this.maxNodeDistance);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
