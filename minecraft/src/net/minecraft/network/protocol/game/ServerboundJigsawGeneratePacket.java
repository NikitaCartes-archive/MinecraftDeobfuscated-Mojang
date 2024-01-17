package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundJigsawGeneratePacket> STREAM_CODEC = Packet.codec(
		ServerboundJigsawGeneratePacket::write, ServerboundJigsawGeneratePacket::new
	);
	private final BlockPos pos;
	private final int levels;
	private final boolean keepJigsaws;

	public ServerboundJigsawGeneratePacket(BlockPos blockPos, int i, boolean bl) {
		this.pos = blockPos;
		this.levels = i;
		this.keepJigsaws = bl;
	}

	private ServerboundJigsawGeneratePacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.levels = friendlyByteBuf.readVarInt();
		this.keepJigsaws = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(this.levels);
		friendlyByteBuf.writeBoolean(this.keepJigsaws);
	}

	@Override
	public PacketType<ServerboundJigsawGeneratePacket> type() {
		return GamePacketTypes.SERVERBOUND_JIGSAW_GENERATE;
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
