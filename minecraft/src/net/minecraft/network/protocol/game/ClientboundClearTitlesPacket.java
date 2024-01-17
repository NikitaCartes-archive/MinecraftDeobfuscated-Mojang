package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundClearTitlesPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundClearTitlesPacket> STREAM_CODEC = Packet.codec(
		ClientboundClearTitlesPacket::write, ClientboundClearTitlesPacket::new
	);
	private final boolean resetTimes;

	public ClientboundClearTitlesPacket(boolean bl) {
		this.resetTimes = bl;
	}

	private ClientboundClearTitlesPacket(FriendlyByteBuf friendlyByteBuf) {
		this.resetTimes = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.resetTimes);
	}

	@Override
	public PacketType<ClientboundClearTitlesPacket> type() {
		return GamePacketTypes.CLIENTBOUND_CLEAR_TITLES;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTitlesClear(this);
	}

	public boolean shouldResetTimes() {
		return this.resetTimes;
	}
}
