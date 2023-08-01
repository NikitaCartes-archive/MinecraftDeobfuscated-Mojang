package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
	private final MinecraftServer server;
	private final Connection connection;

	public MemoryServerHandshakePacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
		this.server = minecraftServer;
		this.connection = connection;
	}

	@Override
	public void handleIntention(ClientIntentionPacket clientIntentionPacket) {
		if (clientIntentionPacket.intention() != ClientIntent.LOGIN) {
			throw new UnsupportedOperationException("Invalid intention " + clientIntentionPacket.intention());
		} else {
			this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
			this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
		}
	}

	@Override
	public void onDisconnect(Component component) {
	}

	@Override
	public boolean isAcceptingMessages() {
		return this.connection.isConnected();
	}
}
