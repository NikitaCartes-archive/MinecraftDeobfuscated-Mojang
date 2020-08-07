package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
	private BlockPos pos;
	private int levels;
	private boolean keepJigsaws;

	public ServerboundJigsawGeneratePacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundJigsawGeneratePacket(BlockPos blockPos, int i, boolean bl) {
		this.pos = blockPos;
		this.levels = i;
		this.keepJigsaws = bl;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.levels = friendlyByteBuf.readVarInt();
		this.keepJigsaws = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
