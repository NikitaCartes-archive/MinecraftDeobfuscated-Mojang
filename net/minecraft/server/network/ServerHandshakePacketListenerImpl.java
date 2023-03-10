/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;

public class ServerHandshakePacketListenerImpl
implements ServerHandshakePacketListener {
    private static final Component IGNORE_STATUS_REASON = Component.literal("Ignoring status request");
    private final MinecraftServer server;
    private final Connection connection;

    public ServerHandshakePacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
        this.server = minecraftServer;
        this.connection = connection;
    }

    @Override
    public void handleIntention(ClientIntentionPacket clientIntentionPacket) {
        switch (clientIntentionPacket.getIntention()) {
            case LOGIN: {
                this.connection.setProtocol(ConnectionProtocol.LOGIN);
                if (clientIntentionPacket.getProtocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
                    MutableComponent component = clientIntentionPacket.getProtocolVersion() < 754 ? Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName()) : Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                    break;
                }
                this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
                break;
            }
            case STATUS: {
                ServerStatus serverStatus = this.server.getStatus();
                if (this.server.repliesToStatus() && serverStatus != null) {
                    this.connection.setProtocol(ConnectionProtocol.STATUS);
                    this.connection.setListener(new ServerStatusPacketListenerImpl(serverStatus, this.connection));
                    break;
                }
                this.connection.disconnect(IGNORE_STATUS_REASON);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + clientIntentionPacket.getIntention());
            }
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

