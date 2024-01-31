package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public record ClientboundDebugSamplePacket(long[] sample, RemoteDebugSampleType debugSampleType) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundDebugSamplePacket> STREAM_CODEC = Packet.codec(
		ClientboundDebugSamplePacket::write, ClientboundDebugSamplePacket::new
	);

	private ClientboundDebugSamplePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readLongArray(), friendlyByteBuf.readEnum(RemoteDebugSampleType.class));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLongArray(this.sample);
		friendlyByteBuf.writeEnum(this.debugSampleType);
	}

	@Override
	public PacketType<ClientboundDebugSamplePacket> type() {
		return GamePacketTypes.CLIENTBOUND_DEBUG_SAMPLE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleDebugSample(this);
	}
}
