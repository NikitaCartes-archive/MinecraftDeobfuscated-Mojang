package net.minecraft.network.protocol;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
	void read(FriendlyByteBuf friendlyByteBuf) throws IOException;

	void write(FriendlyByteBuf friendlyByteBuf) throws IOException;

	void handle(T packetListener);

	default boolean isSkippable() {
		return false;
	}
}
