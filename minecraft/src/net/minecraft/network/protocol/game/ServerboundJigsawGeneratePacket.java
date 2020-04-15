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

	public ServerboundJigsawGeneratePacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundJigsawGeneratePacket(BlockPos blockPos, int i) {
		this.pos = blockPos;
		this.levels = i;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.levels = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(this.levels);
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
}
