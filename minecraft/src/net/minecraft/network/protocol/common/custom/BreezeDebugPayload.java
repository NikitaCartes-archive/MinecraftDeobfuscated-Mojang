package net.minecraft.network.protocol.common.custom;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceLocation;

public record BreezeDebugPayload(BreezeDebugPayload.BreezeInfo breezeInfo) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/breeze");

	public BreezeDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(new BreezeDebugPayload.BreezeInfo(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.breezeInfo.write(friendlyByteBuf);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static record BreezeInfo(UUID uuid, int id, Integer attackTarget, BlockPos jumpTarget) {
		public BreezeInfo(FriendlyByteBuf friendlyByteBuf) {
			this(
				friendlyByteBuf.readUUID(),
				friendlyByteBuf.readInt(),
				friendlyByteBuf.readNullable(FriendlyByteBuf::readInt),
				friendlyByteBuf.readNullable(FriendlyByteBuf::readBlockPos)
			);
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUUID(this.uuid);
			friendlyByteBuf.writeInt(this.id);
			friendlyByteBuf.writeNullable(this.attackTarget, FriendlyByteBuf::writeInt);
			friendlyByteBuf.writeNullable(this.jumpTarget, FriendlyByteBuf::writeBlockPos);
		}

		public String generateName() {
			return DebugEntityNameGenerator.getEntityName(this.uuid);
		}

		public String toString() {
			return this.generateName();
		}
	}
}
