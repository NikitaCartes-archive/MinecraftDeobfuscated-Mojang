package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundAcceptTeleportationPacket> STREAM_CODEC = Packet.codec(
		ServerboundAcceptTeleportationPacket::write, ServerboundAcceptTeleportationPacket::new
	);
	private final int id;

	public ServerboundAcceptTeleportationPacket(int i) {
		this.id = i;
	}

	private ServerboundAcceptTeleportationPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
	}

	@Override
	public PacketType<ServerboundAcceptTeleportationPacket> type() {
		return GamePacketTypes.SERVERBOUND_ACCEPT_TELEPORTATION;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleAcceptTeleportPacket(this);
	}

	public int getId() {
		return this.id;
	}
}
