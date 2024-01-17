package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BrainDebugPayload(BrainDebugPayload.BrainDump brainDump) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, BrainDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BrainDebugPayload::write, BrainDebugPayload::new);
	public static final CustomPacketPayload.Type<BrainDebugPayload> TYPE = CustomPacketPayload.createType("debug/brain");

	private BrainDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(new BrainDebugPayload.BrainDump(friendlyByteBuf));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		this.brainDump.write(friendlyByteBuf);
	}

	@Override
	public CustomPacketPayload.Type<BrainDebugPayload> type() {
		return TYPE;
	}

	public static record BrainDump(
		UUID uuid,
		int id,
		String name,
		String profession,
		int xp,
		float health,
		float maxHealth,
		Vec3 pos,
		String inventory,
		@Nullable Path path,
		boolean wantsGolem,
		int angerLevel,
		List<String> activities,
		List<String> behaviors,
		List<String> memories,
		List<String> gossips,
		Set<BlockPos> pois,
		Set<BlockPos> potentialPois
	) {
		public BrainDump(FriendlyByteBuf friendlyByteBuf) {
			this(
				friendlyByteBuf.readUUID(),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readUtf(),
				friendlyByteBuf.readUtf(),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readFloat(),
				friendlyByteBuf.readFloat(),
				friendlyByteBuf.readVec3(),
				friendlyByteBuf.readUtf(),
				friendlyByteBuf.readNullable(Path::createFromStream),
				friendlyByteBuf.readBoolean(),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readList(FriendlyByteBuf::readUtf),
				friendlyByteBuf.readList(FriendlyByteBuf::readUtf),
				friendlyByteBuf.readList(FriendlyByteBuf::readUtf),
				friendlyByteBuf.readList(FriendlyByteBuf::readUtf),
				friendlyByteBuf.readCollection(HashSet::new, BlockPos.STREAM_CODEC),
				friendlyByteBuf.readCollection(HashSet::new, BlockPos.STREAM_CODEC)
			);
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUUID(this.uuid);
			friendlyByteBuf.writeInt(this.id);
			friendlyByteBuf.writeUtf(this.name);
			friendlyByteBuf.writeUtf(this.profession);
			friendlyByteBuf.writeInt(this.xp);
			friendlyByteBuf.writeFloat(this.health);
			friendlyByteBuf.writeFloat(this.maxHealth);
			friendlyByteBuf.writeVec3(this.pos);
			friendlyByteBuf.writeUtf(this.inventory);
			friendlyByteBuf.writeNullable(this.path, (friendlyByteBufx, path) -> path.writeToStream(friendlyByteBufx));
			friendlyByteBuf.writeBoolean(this.wantsGolem);
			friendlyByteBuf.writeInt(this.angerLevel);
			friendlyByteBuf.writeCollection(this.activities, FriendlyByteBuf::writeUtf);
			friendlyByteBuf.writeCollection(this.behaviors, FriendlyByteBuf::writeUtf);
			friendlyByteBuf.writeCollection(this.memories, FriendlyByteBuf::writeUtf);
			friendlyByteBuf.writeCollection(this.gossips, FriendlyByteBuf::writeUtf);
			friendlyByteBuf.writeCollection(this.pois, BlockPos.STREAM_CODEC);
			friendlyByteBuf.writeCollection(this.potentialPois, BlockPos.STREAM_CODEC);
		}

		public boolean hasPoi(BlockPos blockPos) {
			return this.pois.contains(blockPos);
		}

		public boolean hasPotentialPoi(BlockPos blockPos) {
			return this.potentialPois.contains(blockPos);
		}
	}
}
