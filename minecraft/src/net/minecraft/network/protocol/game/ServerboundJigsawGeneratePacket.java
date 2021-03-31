package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
	private final BlockPos pos;
	private final int levels;
	private final boolean keepJigsaws;

	public ServerboundJigsawGeneratePacket(BlockPos blockPos, int i, boolean bl) {
		this.pos = blockPos;
		this.levels = i;
		this.keepJigsaws = bl;
	}

	public ServerboundJigsawGeneratePacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.levels = friendlyByteBuf.readVarInt();
		this.keepJigsaws = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(this.levels);
		friendlyByteBuf.writeBoolean(this.keepJigsaws);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleJigsawGenerate(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public int levels() {
		return this.levels;
	}

	public boolean keepJigsaws() {
		return this.keepJigsaws;
	}
}
