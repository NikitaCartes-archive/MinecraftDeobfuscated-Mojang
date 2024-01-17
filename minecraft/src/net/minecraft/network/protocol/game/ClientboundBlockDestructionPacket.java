package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundBlockDestructionPacket> STREAM_CODEC = Packet.codec(
		ClientboundBlockDestructionPacket::write, ClientboundBlockDestructionPacket::new
	);
	private final int id;
	private final BlockPos pos;
	private final int progress;

	public ClientboundBlockDestructionPacket(int i, BlockPos blockPos, int j) {
		this.id = i;
		this.pos = blockPos;
		this.progress = j;
	}

	private ClientboundBlockDestructionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.pos = friendlyByteBuf.readBlockPos();
		this.progress = friendlyByteBuf.readUnsignedByte();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeByte(this.progress);
	}

	@Override
	public PacketType<ClientboundBlockDestructionPacket> type() {
		return GamePacketTypes.CLIENTBOUND_BLOCK_DESTRUCTION;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockDestruction(this);
	}

	public int getId() {
		return this.id;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public int getProgress() {
		return this.progress;
	}
}
