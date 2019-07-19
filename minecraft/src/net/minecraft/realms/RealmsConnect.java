package net.minecraft.realms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsConnect {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RealmsScreen onlineScreen;
	private volatile boolean aborted;
	private Connection connection;

	public RealmsConnect(RealmsScreen realmsScreen) {
		this.onlineScreen = realmsScreen;
	}

	public void connect(String string, int i) {
		Realms.setConnectedToRealms(true);
		Realms.narrateNow(Realms.getLocalizedString("mco.connect.success"));
		(new Thread("Realms-connect-task") {
				public void run() {
					InetAddress inetAddress = null;

					try {
						inetAddress = InetAddress.getByName(string);
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.this.connection = Connection.connectToServer(inetAddress, i, Minecraft.getInstance().options.useNativeTransport());
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.this.connection
							.setListener(
								new ClientHandshakePacketListenerImpl(
									RealmsConnect.this.connection, Minecraft.getInstance(), RealmsConnect.this.onlineScreen.getProxy(), component -> {
									}
								)
							);
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.this.connection.send(new ClientIntentionPacket(string, i, ConnectionProtocol.LOGIN));
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.this.connection.send(new ServerboundHelloPacket(Minecraft.getInstance().getUser().getGameProfile()));
					} catch (UnknownHostException var5) {
						Realms.clearResourcePack();
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var5);
						Realms.setScreen(
							new DisconnectedRealmsScreen(
								RealmsConnect.this.onlineScreen, "connect.failed", new TranslatableComponent("disconnect.genericReason", "Unknown host '" + string + "'")
							)
						);
					} catch (Exception var6) {
						Realms.clearResourcePack();
						if (RealmsConnect.this.aborted) {
							return;
						}

						RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var6);
						String string = var6.toString();
						if (inetAddress != null) {
							String string2 = inetAddress + ":" + i;
							string = string.replaceAll(string2, "");
						}

						Realms.setScreen(
							new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, "connect.failed", new TranslatableComponent("disconnect.genericReason", string))
						);
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
