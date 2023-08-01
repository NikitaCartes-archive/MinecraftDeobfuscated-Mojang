package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

public class ServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
	private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
	private final MinecraftServer server;
	private final Connection connection;

	public ServerHandshakePacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
		this.server = minecraftServer;
		this.connection = connection;
	}

	@Override
	public void handleIntention(ClientIntentionPacket clientIntentionPacket) {
		switch (clientIntentionPacket.intention()) {
			case LOGIN:
				this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
				if (clientIntentionPacket.protocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
					Component component;
					if (clientIntentionPacket.protocolVersion() < 754) {
						component = Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName());
					} else {
						component = Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
					}

					this.connection.send(new ClientboundLoginDisconnectPacket(component));
					this.connection.disconnect(component);
				} else {
					this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
				}
				break;
			case STATUS:
				ServerStatus serverStatus = this.server.getStatus();
				if (this.server.repliesToStatus() && serverStatus != null) {
					this.connection.setClientboundProtocolAfterHandshake(ClientIntent.STATUS);
					this.connection.setListener(new ServerStatusPacketListenerImpl(serverStatus, this.connection));
				} else {
					this.connection.disconnect(IGNORE_STATUS_REASON);
				}
				break;
			default:
				throw new UnsupportedOperationException("Invalid intention " + clientIntentionPacket.intention());
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
