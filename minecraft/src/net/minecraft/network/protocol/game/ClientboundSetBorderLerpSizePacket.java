package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderLerpSizePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderLerpSizePacket> STREAM_CODEC = Packet.codec(
		ClientboundSetBorderLerpSizePacket::write, ClientboundSetBorderLerpSizePacket::new
	);
	private final double oldSize;
	private final double newSize;
	private final long lerpTime;

	public ClientboundSetBorderLerpSizePacket(WorldBorder worldBorder) {
		this.oldSize = worldBorder.getSize();
		this.newSize = worldBorder.getLerpTarget();
		this.lerpTime = worldBorder.getLerpRemainingTime();
	}

	private ClientboundSetBorderLerpSizePacket(FriendlyByteBuf friendlyByteBuf) {
		this.oldSize = friendlyByteBuf.readDouble();
		this.newSize = friendlyByteBuf.readDouble();
		this.lerpTime = friendlyByteBuf.readVarLong();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.oldSize);
		friendlyByteBuf.writeDouble(this.newSize);
		friendlyByteBuf.writeVarLong(this.lerpTime);
	}

	@Override
	public PacketType<ClientboundSetBorderLerpSizePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_BORDER_LERP_SIZE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderLerpSize(this);
	}

	public double getOldSize() {
		return this.oldSize;
	}

	public double getNewSize() {
		return this.newSize;
	}

	public long getLerpTime() {
		return this.lerpTime;
	}
}
