package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
	private int id;
	private BlockPos pos;
	private int progress;

	public ClientboundBlockDestructionPacket() {
	}

	public ClientboundBlockDestructionPacket(int i, BlockPos blockPos, int j) {
		this.id = i;
		this.pos = blockPos;
		this.progress = j;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.pos = friendlyByteBuf.readBlockPos();
		this.progress = friendlyByteBuf.readUnsignedByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeByte(this.progress);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockDestruction(this);
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}

	@Environment(EnvType.CLIENT)
	public int getProgress() {
		return this.progress;
	}
}
