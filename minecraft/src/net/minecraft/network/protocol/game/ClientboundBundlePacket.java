package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;

public class ClientboundBundlePacket extends BundlePacket<ClientGamePacketListener> {
	public ClientboundBundlePacket(Iterable<Packet<ClientGamePacketListener>> iterable) {
		super(iterable);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBundlePacket(this);
	}
}
