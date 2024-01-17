package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundBundlePacket extends BundlePacket<ClientGamePacketListener> {
	public ClientboundBundlePacket(Iterable<Packet<? super ClientGamePacketListener>> iterable) {
		super(iterable);
	}

	@Override
	public PacketType<ClientboundBundlePacket> type() {
		return GamePacketTypes.CLIENTBOUND_BUNDLE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBundlePacket(this);
	}
}
