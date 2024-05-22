package net.minecraft.network.protocol.handshake;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class HandshakePacketTypes {
	public static final PacketType<ClientIntentionPacket> CLIENT_INTENTION = createServerbound("intention");

	private static <T extends Packet<ServerHandshakePacketListener>> PacketType<T> createServerbound(String string) {
		return new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(string));
	}
}
