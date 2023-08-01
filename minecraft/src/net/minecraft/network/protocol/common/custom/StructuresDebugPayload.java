package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record StructuresDebugPayload(ResourceKey<Level> dimension, BoundingBox mainBB, List<StructuresDebugPayload.PieceInfo> pieces)
	implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/structures");

	public StructuresDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readResourceKey(Registries.DIMENSION), readBoundingBox(friendlyByteBuf), friendlyByteBuf.readList(StructuresDebugPayload.PieceInfo::new));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceKey(this.dimension);
		writeBoundingBox(friendlyByteBuf, this.mainBB);
		friendlyByteBuf.writeCollection(this.pieces, (friendlyByteBuf2, pieceInfo) -> pieceInfo.write(friendlyByteBuf));
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	static BoundingBox readBoundingBox(FriendlyByteBuf friendlyByteBuf) {
		return new BoundingBox(
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readInt()
		);
	}

	static void writeBoundingBox(FriendlyByteBuf friendlyByteBuf, BoundingBox boundingBox) {
		friendlyByteBuf.writeInt(boundingBox.minX());
		friendlyByteBuf.writeInt(boundingBox.minY());
		friendlyByteBuf.writeInt(boundingBox.minZ());
		friendlyByteBuf.writeInt(boundingBox.maxX());
		friendlyByteBuf.writeInt(boundingBox.maxY());
		friendlyByteBuf.writeInt(boundingBox.maxZ());
	}

	public static record PieceInfo(BoundingBox boundingBox, boolean isStart) {
		public PieceInfo(FriendlyByteBuf friendlyByteBuf) {
			this(StructuresDebugPayload.readBoundingBox(friendlyByteBuf), friendlyByteBuf.readBoolean());
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			StructuresDebugPayload.writeBoundingBox(friendlyByteBuf, this.boundingBox);
			friendlyByteBuf.writeBoolean(this.isStart);
		}
	}
}
