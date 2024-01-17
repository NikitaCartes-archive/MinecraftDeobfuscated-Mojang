package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundSignUpdatePacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundSignUpdatePacket> STREAM_CODEC = Packet.codec(
		ServerboundSignUpdatePacket::write, ServerboundSignUpdatePacket::new
	);
	private static final int MAX_STRING_LENGTH = 384;
	private final BlockPos pos;
	private final String[] lines;
	private final boolean isFrontText;

	public ServerboundSignUpdatePacket(BlockPos blockPos, boolean bl, String string, String string2, String string3, String string4) {
		this.pos = blockPos;
		this.isFrontText = bl;
		this.lines = new String[]{string, string2, string3, string4};
	}

	private ServerboundSignUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.isFrontText = friendlyByteBuf.readBoolean();
		this.lines = new String[4];

		for (int i = 0; i < 4; i++) {
			this.lines[i] = friendlyByteBuf.readUtf(384);
		}
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeBoolean(this.isFrontText);

		for (int i = 0; i < 4; i++) {
			friendlyByteBuf.writeUtf(this.lines[i]);
		}
	}

	@Override
	public PacketType<ServerboundSignUpdatePacket> type() {
		return GamePacketTypes.SERVERBOUND_SIGN_UPDATE;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSignUpdate(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public boolean isFrontText() {
		return this.isFrontText;
	}

	public String[] getLines() {
		return this.lines;
	}
}
