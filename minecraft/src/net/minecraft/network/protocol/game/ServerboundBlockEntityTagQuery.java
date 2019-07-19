package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundBlockEntityTagQuery implements Packet<ServerGamePacketListener> {
	private int transactionId;
	private BlockPos pos;

	public ServerboundBlockEntityTagQuery() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundBlockEntityTagQuery(int i, BlockPos blockPos) {
		this.transactionId = i;
		this.pos = blockPos;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.transactionId = friendlyByteBuf.readVarInt();
		this.pos = friendlyByteBuf.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeBlockPos(this.pos);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleBlockEntityTagQuery(this);
	}

	public int getTransactionId() {
		return this.transactionId;
	}

	public BlockPos getPos() {
		return this.pos;
	}
}
