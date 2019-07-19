package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket implements Packet<ClientGamePacketListener> {
	private BlockPos pos;
	private int b0;
	private int b1;
	private Block block;

	public ClientboundBlockEventPacket() {
	}

	public ClientboundBlockEventPacket(BlockPos blockPos, Block block, int i, int j) {
		this.pos = blockPos;
		this.block = block;
		this.b0 = i;
		this.b1 = j;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.b0 = friendlyByteBuf.readUnsignedByte();
		this.b1 = friendlyByteBuf.readUnsignedByte();
		this.block = Registry.BLOCK.byId(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeByte(this.b0);
		friendlyByteBuf.writeByte(this.b1);
		friendlyByteBuf.writeVarInt(Registry.BLOCK.getId(this.block));
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockEvent(this);
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}

	@Environment(EnvType.CLIENT)
	public int getB0() {
		return this.b0;
	}

	@Environment(EnvType.CLIENT)
	public int getB1() {
		return this.b1;
	}

	@Environment(EnvType.CLIENT)
	public Block getBlock() {
		return this.block;
	}
}
