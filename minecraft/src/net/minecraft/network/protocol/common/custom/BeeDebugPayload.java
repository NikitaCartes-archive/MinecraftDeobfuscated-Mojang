package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BeeDebugPayload(BeeDebugPayload.BeeInfo beeInfo) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, BeeDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BeeDebugPayload::write, BeeDebugPayload::new);
	public static final CustomPacketPayload.Type<BeeDebugPayload> TYPE = CustomPacketPayload.createType("debug/bee");

	private BeeDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(new BeeDebugPayload.BeeInfo(friendlyByteBuf));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		this.beeInfo.write(friendlyByteBuf);
	}

	@Override
	public CustomPacketPayload.Type<BeeDebugPayload> type() {
		return TYPE;
	}

	public static record BeeInfo(
		UUID uuid,
		int id,
		Vec3 pos,
		@Nullable Path path,
		@Nullable BlockPos hivePos,
		@Nullable BlockPos flowerPos,
		int travelTicks,
		Set<String> goals,
		List<BlockPos> blacklistedHives
	) {
		public BeeInfo(FriendlyByteBuf friendlyByteBuf) {
			this(
				friendlyByteBuf.readUUID(),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readVec3(),
				friendlyByteBuf.readNullable(Path::createFromStream),
				friendlyByteBuf.readNullable(BlockPos.STREAM_CODEC),
				friendlyByteBuf.readNullable(BlockPos.STREAM_CODEC),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readUtf),
				friendlyByteBuf.readList(BlockPos.STREAM_CODEC)
			);
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUUID(this.uuid);
			friendlyByteBuf.writeInt(this.id);
			friendlyByteBuf.writeVec3(this.pos);
			friendlyByteBuf.writeNullable(this.path, (friendlyByteBufx, path) -> path.writeToStream(friendlyByteBufx));
			friendlyByteBuf.writeNullable(this.hivePos, BlockPos.STREAM_CODEC);
			friendlyByteBuf.writeNullable(this.flowerPos, BlockPos.STREAM_CODEC);
			friendlyByteBuf.writeInt(this.travelTicks);
			friendlyByteBuf.writeCollection(this.goals, FriendlyByteBuf::writeUtf);
			friendlyByteBuf.writeCollection(this.blacklistedHives, BlockPos.STREAM_CODEC);
		}

		public boolean hasHive(BlockPos blockPos) {
			return Objects.equals(blockPos, this.hivePos);
		}

		public String generateName() {
			return DebugEntityNameGenerator.getEntityName(this.uuid);
		}

		public String toString() {
			return this.generateName();
		}
	}
}
