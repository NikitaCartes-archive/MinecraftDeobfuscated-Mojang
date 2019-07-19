package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.server.MinecraftServer;

public class ServerStatusPacketListenerImpl implements ServerStatusPacketListener {
	private static final Component DISCONNECT_REASON = new TranslatableComponent("multiplayer.status.request_handled");
	private final MinecraftServer server;
	private final Connection connection;
	private boolean hasRequestedStatus;

	public ServerStatusPacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
		this.server = minecraftServer;
		this.connection = connection;
	}

	@Override
	public void onDisconnect(Component component) {
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	@Override
	public void handleStatusRequest(ServerboundStatusRequestPacket serverboundStatusRequestPacket) {
		if (this.hasRequestedStatus) {
			this.connection.disconnect(DISCONNECT_REASON);
		} else {
			this.hasRequestedStatus = true;
			this.connection.send(new ClientboundStatusResponsePacket(this.server.getStatus()));
		}
	}

	@Override
	public void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket) {
		this.connection.send(new ClientboundPongResponsePacket(serverboundPingRequestPacket.getTime()));
		this.connection.disconnect(DISCONNECT_REASON);
	}
}
