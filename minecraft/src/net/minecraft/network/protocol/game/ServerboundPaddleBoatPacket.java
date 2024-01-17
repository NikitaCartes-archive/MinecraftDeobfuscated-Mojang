package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundPaddleBoatPacket> STREAM_CODEC = Packet.codec(
		ServerboundPaddleBoatPacket::write, ServerboundPaddleBoatPacket::new
	);
	private final boolean left;
	private final boolean right;

	public ServerboundPaddleBoatPacket(boolean bl, boolean bl2) {
		this.left = bl;
		this.right = bl2;
	}

	private ServerboundPaddleBoatPacket(FriendlyByteBuf friendlyByteBuf) {
		this.left = friendlyByteBuf.readBoolean();
		this.right = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.left);
		friendlyByteBuf.writeBoolean(this.right);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePaddleBoat(this);
	}

	@Override
	public PacketType<ServerboundPaddleBoatPacket> type() {
		return GamePacketTypes.SERVERBOUND_PADDLE_BOAT;
	}

	public boolean getLeft() {
		return this.left;
	}

	public boolean getRight() {
		return this.right;
	}
}
