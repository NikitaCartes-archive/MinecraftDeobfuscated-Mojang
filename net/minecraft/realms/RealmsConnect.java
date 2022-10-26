/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsConnect {
    static final Logger LOGGER = LogUtils.getLogger();
    final Screen onlineScreen;
    volatile boolean aborted;
    @Nullable
    Connection connection;

    public RealmsConnect(Screen screen) {
        this.onlineScreen = screen;
    }

    public void connect(final RealmsServer realmsServer, ServerAddress serverAddress) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.setConnectedToRealms(true);
        minecraft.prepareForMultiplayer();
        minecraft.getNarrator().sayNow(Component.translatable("mco.connect.success"));
        final String string = serverAddress.getHost();
        final int i = serverAddress.getPort();
        new Thread("Realms-connect-task"){

            @Override
            public void run() {
                InetSocketAddress inetSocketAddress = null;
                try {
                    inetSocketAddress = new InetSocketAddress(string, i);
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.setListener(new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, minecraft, realmsServer.toServerData(string), RealmsConnect.this.onlineScreen, component -> {}));
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.send(new ClientIntentionPacket(string, i, ConnectionProtocol.LOGIN));
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    String string4 = minecraft.getUser().getName();
                    UUID uUID = minecraft.getUser().getProfileId();
                    RealmsConnect.this.connection.send(new ServerboundHelloPacket(string4, Optional.ofNullable(uUID)));
                    minecraft.updateReportEnvironment(ReportEnvironment.realm(realmsServer));
                } catch (Exception exception) {
                    minecraft.getDownloadedPackSource().clearServerPack();
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to world", exception);
                    String string2 = exception.toString();
                    if (inetSocketAddress != null) {
                        String string3 = inetSocketAddress + ":" + i;
                        string2 = string2.replaceAll(string3, "");
                    }
                    DisconnectedRealmsScreen disconnectedRealmsScreen = new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", string2));
                    minecraft.execute(() -> minecraft.setScreen(disconnectedRealmsScreen));
                }
            }
        }.start();
    }

    public void abort() {
        this.aborted = true;
        if (this.connection != null && this.connection.isConnected()) {
            this.connection.disconnect(Component.translatable("disconnect.genericReason"));
            this.connection.handleDisconnection();
        }
    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }
    }
}

