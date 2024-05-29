package net.minecraft.network.protocol.handshake;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public class HandshakeProtocols {
	public static final ProtocolInfo.Unbound<ServerHandshakePacketListener, FriendlyByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
		ConnectionProtocol.HANDSHAKING,
		protocolInfoBuilder -> protocolInfoBuilder.addPacket(HandshakePacketTypes.CLIENT_INTENTION, ClientIntentionPacket.STREAM_CODEC)
	);
	public static final ProtocolInfo<ServerHandshakePacketListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
}
