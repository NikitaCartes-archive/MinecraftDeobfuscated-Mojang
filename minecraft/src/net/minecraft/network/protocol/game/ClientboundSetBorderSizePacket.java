package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderSizePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderSizePacket> STREAM_CODEC = Packet.codec(
		ClientboundSetBorderSizePacket::write, ClientboundSetBorderSizePacket::new
	);
	private final double size;

	public ClientboundSetBorderSizePacket(WorldBorder worldBorder) {
		this.size = worldBorder.getLerpTarget();
	}

	private ClientboundSetBorderSizePacket(FriendlyByteBuf friendlyByteBuf) {
		this.size = friendlyByteBuf.readDouble();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.size);
	}

	@Override
	public PacketType<ClientboundSetBorderSizePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_BORDER_SIZE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderSize(this);
	}

	public double getSize() {
		return this.size;
	}
}
