package net.minecraft.network.protocol;

import javax.annotation.Nullable;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
	void write(FriendlyByteBuf friendlyByteBuf);

	void handle(T packetListener);

	default boolean isSkippable() {
		return false;
	}

	@Nullable
	default ConnectionProtocol nextProtocol() {
		return null;
	}
}
