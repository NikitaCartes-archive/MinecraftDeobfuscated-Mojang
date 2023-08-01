package net.minecraft.network;

import net.minecraft.network.protocol.PacketFlow;

public interface ClientboundPacketListener extends PacketListener {
	@Override
	default PacketFlow flow() {
		return PacketFlow.CLIENTBOUND;
	}
}
