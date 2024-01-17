package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDistancePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderWarningDistancePacket> STREAM_CODEC = Packet.codec(
		ClientboundSetBorderWarningDistancePacket::write, ClientboundSetBorderWarningDistancePacket::new
	);
	private final int warningBlocks;

	public ClientboundSetBorderWarningDistancePacket(WorldBorder worldBorder) {
		this.warningBlocks = worldBorder.getWarningBlocks();
	}

	private ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf friendlyByteBuf) {
		this.warningBlocks = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.warningBlocks);
	}

	@Override
	public PacketType<ClientboundSetBorderWarningDistancePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DISTANCE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderWarningDistance(this);
	}

	public int getWarningBlocks() {
		return this.warningBlocks;
	}
}
