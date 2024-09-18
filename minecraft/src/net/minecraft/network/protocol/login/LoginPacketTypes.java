package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class LoginPacketTypes {
	public static final PacketType<ClientboundCustomQueryPacket> CLIENTBOUND_CUSTOM_QUERY = createClientbound("custom_query");
	public static final PacketType<ClientboundLoginFinishedPacket> CLIENTBOUND_LOGIN_FINISHED = createClientbound("login_finished");
	public static final PacketType<ClientboundHelloPacket> CLIENTBOUND_HELLO = createClientbound("hello");
	public static final PacketType<ClientboundLoginCompressionPacket> CLIENTBOUND_LOGIN_COMPRESSION = createClientbound("login_compression");
	public static final PacketType<ClientboundLoginDisconnectPacket> CLIENTBOUND_LOGIN_DISCONNECT = createClientbound("login_disconnect");
	public static final PacketType<ServerboundCustomQueryAnswerPacket> SERVERBOUND_CUSTOM_QUERY_ANSWER = createServerbound("custom_query_answer");
	public static final PacketType<ServerboundHelloPacket> SERVERBOUND_HELLO = createServerbound("hello");
	public static final PacketType<ServerboundKeyPacket> SERVERBOUND_KEY = createServerbound("key");
	public static final PacketType<ServerboundLoginAcknowledgedPacket> SERVERBOUND_LOGIN_ACKNOWLEDGED = createServerbound("login_acknowledged");

	private static <T extends Packet<ClientLoginPacketListener>> PacketType<T> createClientbound(String string) {
		return new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace(string));
	}

	private static <T extends Packet<ServerLoginPacketListener>> PacketType<T> createServerbound(String string) {
		return new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(string));
	}
}
