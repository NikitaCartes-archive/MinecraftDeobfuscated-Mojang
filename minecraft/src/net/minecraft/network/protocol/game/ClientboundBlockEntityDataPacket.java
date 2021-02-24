package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
	private final BlockPos pos;
	private final int type;
	private final CompoundTag tag;

	public ClientboundBlockEntityDataPacket(BlockPos blockPos, int i, CompoundTag compoundTag) {
		this.pos = blockPos;
		this.type = i;
		this.tag = compoundTag;
	}

	public ClientboundBlockEntityDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.type = friendlyByteBuf.readUnsignedByte();
		this.tag = friendlyByteBuf.readNbt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeByte((byte)this.type);
		friendlyByteBuf.writeNbt(this.tag);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockEntityData(this);
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}

	@Environment(EnvType.CLIENT)
	public int getType() {
		return this.type;
	}

	@Environment(EnvType.CLIENT)
	public CompoundTag getTag() {
		return this.tag;
	}
}
