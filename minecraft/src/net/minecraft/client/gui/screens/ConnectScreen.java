package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ConnectScreen extends Screen {
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	private static final Logger LOGGER = LogManager.getLogger();
	private Connection connection;
	private boolean aborted;
	private final Screen parent;
	private Component status = new TranslatableComponent("connect.connecting");
	private long lastNarration = -1L;

	public ConnectScreen(Screen screen, Minecraft minecraft, ServerData serverData) {
		super(NarratorChatListener.NO_TITLE);
		this.minecraft = minecraft;
		this.parent = screen;
		ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
		minecraft.clearLevel();
		minecraft.setCurrentServer(serverData);
		this.connect(serverAddress.getHost(), serverAddress.getPort());
	}

	public ConnectScreen(Screen screen, Minecraft minecraft, String string, int i) {
		super(NarratorChatListener.NO_TITLE);
		this.minecraft = minecraft;
		this.parent = screen;
		minecraft.clearLevel();
		this.connect(string, i);
	}

	private void connect(String string, int i) {
		LOGGER.info("Connecting to {}, {}", string, i);
		Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
			public void run() {
				InetAddress inetAddress = null;

				try {
					if (ConnectScreen.this.aborted) {
						return;
					}

					inetAddress = InetAddress.getByName(string);
					ConnectScreen.this.connection = Connection.connectToServer(inetAddress, i, ConnectScreen.this.minecraft.options.useNativeTransport());
					ConnectScreen.this.connection
						.setListener(
							new ClientHandshakePacketListenerImpl(
								ConnectScreen.this.connection, ConnectScreen.this.minecraft, ConnectScreen.this.parent, component -> ConnectScreen.this.updateStatus(component)
							)
						);
					ConnectScreen.this.connection.send(new ClientIntentionPacket(string, i, ConnectionProtocol.LOGIN));
					ConnectScreen.this.connection.send(new ServerboundHelloPacket(ConnectScreen.this.minecraft.getUser().getGameProfile()));
				} catch (UnknownHostException var4) {
					if (ConnectScreen.this.aborted) {
						return;
					}

					ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var4);
					ConnectScreen.this.minecraft
						.execute(
							() -> ConnectScreen.this.minecraft
									.setScreen(
										new DisconnectedScreen(
											ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", "Unknown host")
										)
									)
						);
				} catch (Exception var5) {
					if (ConnectScreen.this.aborted) {
						return;
					}

					ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var5);
					String string = inetAddress == null ? var5.toString() : var5.toString().replaceAll(inetAddress + ":" + i, "");
					ConnectScreen.this.minecraft
						.execute(
							() -> ConnectScreen.this.minecraft
									.setScreen(
										new DisconnectedScreen(ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", string))
									)
						);
				}
			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
	}

	private void updateStatus(Component component) {
		this.status = component;
	}

	@Override
	public void tick() {
		if (this.connection != null) {
			if (this.connection.isConnected()) {
				this.connection.tick();
			} else {
				this.connection.handleDisconnection();
			}
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void init() {
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, button -> {
			this.aborted = true;
			if (this.connection != null) {
				this.connection.disconnect(new TranslatableComponent("connect.aborted"));
			}

			this.minecraft.setScreen(this.parent);
		}));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		long l = Util.getMillis();
		if (l - this.lastNarration > 2000L) {
			this.lastNarration = l;
			NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.joining").getString());
		}

		drawCenteredString(poseStack, this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
		super.render(poseStack, i, j, f);
	}
}
