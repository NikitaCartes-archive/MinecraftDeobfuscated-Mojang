package net.minecraft.realms;

import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsConnect {
	static final Logger LOGGER = LogManager.getLogger();
	final Screen onlineScreen;
	volatile boolean aborted;
	Connection connection;

	public RealmsConnect(Screen screen) {
		this.onlineScreen = screen;
	}

	public void connect(RealmsServer realmsServer, ServerAddress serverAddress) {
		final Minecraft minecraft = Minecraft.getInstance();
		minecraft.setConnectedToRealms(true);
		NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("mco.connect.success"));
		final String string = serverAddress.getHost();
		final int i = serverAddress.getPort();
		(new Thread("Realms-connect-task") {
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

						RealmsConnect.this.connection
							.setListener(new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, minecraft, RealmsConnect.this.onlineScreen, component -> {
							}));
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.this.connection.send(new ClientIntentionPacket(string, i, ConnectionProtocol.LOGIN));
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
						minecraft.setCurrentServer(realmsServer.toServerData(string));
					} catch (Exception var5) {
						minecraft.getClientPackSource().clearServerPack();
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var5);
						String string = var5.toString();
						if (inetSocketAddress != null) {
							String string2 = inetSocketAddress + ":" + i;
							string = string.replaceAll(string2, "");
						}

						DisconnectedRealmsScreen disconnectedRealmsScreen = new DisconnectedRealmsScreen(
							RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", string)
						);
						minecraft.execute(() -> minecraft.setScreen(disconnectedRealmsScreen));
					}
				}
			})
			.start();
	}

	public void abort() {
		this.aborted = true;
		if (this.connection != null && this.connection.isConnected()) {
			this.connection.disconnect(new TranslatableComponent("disconnect.genericReason"));
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
