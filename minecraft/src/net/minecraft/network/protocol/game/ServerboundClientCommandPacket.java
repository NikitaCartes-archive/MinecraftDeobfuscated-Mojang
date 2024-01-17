package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundClientCommandPacket> STREAM_CODEC = Packet.codec(
		ServerboundClientCommandPacket::write, ServerboundClientCommandPacket::new
	);
	private final ServerboundClientCommandPacket.Action action;

	public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action action) {
		this.action = action;
	}

	private ServerboundClientCommandPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ServerboundClientCommandPacket.Action.class);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
	}

	@Override
	public PacketType<ServerboundClientCommandPacket> type() {
		return GamePacketTypes.SERVERBOUND_CLIENT_COMMAND;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleClientCommand(this);
	}

	public ServerboundClientCommandPacket.Action getAction() {
		return this.action;
	}

	public static enum Action {
		PERFORM_RESPAWN,
		REQUEST_STATS;
	}
}
