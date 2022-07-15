package net.minecraft.network;

public interface TickablePacketListener extends PacketListener {
	void tick();
}
