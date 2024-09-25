package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundSetTimePacket(long gameTime, long dayTime, boolean tickDayTime) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetTimePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.LONG,
		ClientboundSetTimePacket::gameTime,
		ByteBufCodecs.LONG,
		ClientboundSetTimePacket::dayTime,
		ByteBufCodecs.BOOL,
		ClientboundSetTimePacket::tickDayTime,
		ClientboundSetTimePacket::new
	);

	@Override
	public PacketType<ClientboundSetTimePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_TIME;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetTime(this);
	}
}
