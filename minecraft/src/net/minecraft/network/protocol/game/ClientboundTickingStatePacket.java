package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<ClientGamePacketListener> {
	public ClientboundTickingStatePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readFloat(), friendlyByteBuf.readBoolean());
	}

	public static ClientboundTickingStatePacket from(TickRateManager tickRateManager) {
		return new ClientboundTickingStatePacket(tickRateManager.tickrate(), tickRateManager.isFrozen());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.tickRate);
		friendlyByteBuf.writeBoolean(this.isFrozen);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTickingState(this);
	}
}
