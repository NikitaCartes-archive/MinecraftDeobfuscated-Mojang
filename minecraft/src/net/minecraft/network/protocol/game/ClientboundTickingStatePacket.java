package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundTickingStatePacket> STREAM_CODEC = Packet.codec(
		ClientboundTickingStatePacket::write, ClientboundTickingStatePacket::new
	);

	private ClientboundTickingStatePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readFloat(), friendlyByteBuf.readBoolean());
	}

	public static ClientboundTickingStatePacket from(TickRateManager tickRateManager) {
		return new ClientboundTickingStatePacket(tickRateManager.tickrate(), tickRateManager.isFrozen());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.tickRate);
		friendlyByteBuf.writeBoolean(this.isFrozen);
	}

	@Override
	public PacketType<ClientboundTickingStatePacket> type() {
		return GamePacketTypes.CLIENTBOUND_TICKING_STATE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTickingState(this);
	}
}
