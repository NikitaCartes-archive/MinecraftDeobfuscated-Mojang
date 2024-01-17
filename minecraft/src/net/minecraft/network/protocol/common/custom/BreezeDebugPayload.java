package net.minecraft.network.protocol.common.custom;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;

public record BreezeDebugPayload(BreezeDebugPayload.BreezeInfo breezeInfo) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, BreezeDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
		BreezeDebugPayload::write, BreezeDebugPayload::new
	);
	public static final CustomPacketPayload.Type<BreezeDebugPayload> TYPE = CustomPacketPayload.createType("debug/breeze");

	private BreezeDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(new BreezeDebugPayload.BreezeInfo(friendlyByteBuf));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		this.breezeInfo.write(friendlyByteBuf);
	}

	@Override
	public CustomPacketPayload.Type<BreezeDebugPayload> type() {
		return TYPE;
	}

	public static record BreezeInfo(UUID uuid, int id, Integer attackTarget, BlockPos jumpTarget) {
		public BreezeInfo(FriendlyByteBuf friendlyByteBuf) {
			this(
				friendlyByteBuf.readUUID(),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readNullable(FriendlyByteBuf::readInt),
				friendlyByteBuf.readNullable(BlockPos.STREAM_CODEC)
			);
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUUID(this.uuid);
			friendlyByteBuf.writeInt(this.id);
			friendlyByteBuf.writeNullable(this.attackTarget, FriendlyByteBuf::writeInt);
			friendlyByteBuf.writeNullable(this.jumpTarget, BlockPos.STREAM_CODEC);
		}

		public String generateName() {
			return DebugEntityNameGenerator.getEntityName(this.uuid);
		}

		public String toString() {
			return this.generateName();
		}
	}
}
