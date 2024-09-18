package net.minecraft.network.protocol.login;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;

public class LoginProtocols {
	public static final ProtocolInfo.Unbound<ServerLoginPacketListener, FriendlyByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
		ConnectionProtocol.LOGIN,
		protocolInfoBuilder -> protocolInfoBuilder.addPacket(LoginPacketTypes.SERVERBOUND_HELLO, ServerboundHelloPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.SERVERBOUND_KEY, ServerboundKeyPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.SERVERBOUND_CUSTOM_QUERY_ANSWER, ServerboundCustomQueryAnswerPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.SERVERBOUND_LOGIN_ACKNOWLEDGED, ServerboundLoginAcknowledgedPacket.STREAM_CODEC)
				.addPacket(CookiePacketTypes.SERVERBOUND_COOKIE_RESPONSE, ServerboundCookieResponsePacket.STREAM_CODEC)
	);
	public static final ProtocolInfo<ServerLoginPacketListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
	public static final ProtocolInfo.Unbound<ClientLoginPacketListener, FriendlyByteBuf> CLIENTBOUND_TEMPLATE = ProtocolInfoBuilder.clientboundProtocol(
		ConnectionProtocol.LOGIN,
		protocolInfoBuilder -> protocolInfoBuilder.addPacket(LoginPacketTypes.CLIENTBOUND_LOGIN_DISCONNECT, ClientboundLoginDisconnectPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.CLIENTBOUND_HELLO, ClientboundHelloPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.CLIENTBOUND_LOGIN_FINISHED, ClientboundLoginFinishedPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.CLIENTBOUND_LOGIN_COMPRESSION, ClientboundLoginCompressionPacket.STREAM_CODEC)
				.addPacket(LoginPacketTypes.CLIENTBOUND_CUSTOM_QUERY, ClientboundCustomQueryPacket.STREAM_CODEC)
				.addPacket(CookiePacketTypes.CLIENTBOUND_COOKIE_REQUEST, ClientboundCookieRequestPacket.STREAM_CODEC)
	);
	public static final ProtocolInfo<ClientLoginPacketListener> CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
}
