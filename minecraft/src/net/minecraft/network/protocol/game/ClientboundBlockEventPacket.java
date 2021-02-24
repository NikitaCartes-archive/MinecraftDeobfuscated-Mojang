package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket implements Packet<ClientGamePacketListener> {
	private final BlockPos pos;
	private final int b0;
	private final int b1;
	private final Block block;

	public ClientboundBlockEventPacket(BlockPos blockPos, Block block, int i, int j) {
		this.pos = blockPos;
		this.block = block;
		this.b0 = i;
		this.b1 = j;
	}

	public ClientboundBlockEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.b0 = friendlyByteBuf.readUnsignedByte();
		this.b1 = friendlyByteBuf.readUnsignedByte();
		this.block = Registry.BLOCK.byId(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
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
