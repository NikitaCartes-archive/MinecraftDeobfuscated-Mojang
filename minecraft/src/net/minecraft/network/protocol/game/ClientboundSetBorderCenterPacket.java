package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderCenterPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderCenterPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetBorderCenterPacket::write, ClientboundSetBorderCenterPacket::new
	);
	private final double newCenterX;
	private final double newCenterZ;

	public ClientboundSetBorderCenterPacket(WorldBorder worldBorder) {
		this.newCenterX = worldBorder.getCenterX();
		this.newCenterZ = worldBorder.getCenterZ();
	}

	private ClientboundSetBorderCenterPacket(FriendlyByteBuf friendlyByteBuf) {
		this.newCenterX = friendlyByteBuf.readDouble();
		this.newCenterZ = friendlyByteBuf.readDouble();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.newCenterX);
		friendlyByteBuf.writeDouble(this.newCenterZ);
	}

	@Override
	public PacketType<ClientboundSetBorderCenterPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_BORDER_CENTER;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderCenter(this);
	}

	public double getNewCenterZ() {
		return this.newCenterZ;
	}

	public double getNewCenterX() {
		return this.newCenterX;
	}
}
