package net.minecraft.client.gui.screens;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ConnectScreen extends Screen {
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	static final Logger LOGGER = LogUtils.getLogger();
	private static final long NARRATION_DELAY_MS = 2000L;
	public static final Component ABORT_CONNECTION = Component.translatable("connect.aborted");
	public static final Component UNKNOWN_HOST_MESSAGE = Component.translatable("disconnect.genericReason", Component.translatable("disconnect.unknownHost"));
	@Nullable
	volatile Connection connection;
	@Nullable
	ChannelFuture channelFuture;
	volatile boolean aborted;
	final Screen parent;
	private Component status = Component.translatable("connect.connecting");
	private long lastNarration = -1L;
	final Component connectFailedTitle;

	private ConnectScreen(Screen screen, Component component) {
		super(GameNarrator.NO_TITLE);
		this.parent = screen;
		this.connectFailedTitle = component;
	}

	public static void startConnecting(Screen screen, Minecraft minecraft, ServerAddress serverAddress, ServerData serverData, boolean bl) {
		if (minecraft.screen instanceof ConnectScreen) {
			LOGGER.error("Attempt to connect while already connecting");
		} else {
			ConnectScreen connectScreen = new ConnectScreen(screen, bl ? QuickPlay.ERROR_TITLE : CommonComponents.CONNECT_FAILED);
			minecraft.disconnect();
			minecraft.prepareForMultiplayer();
			minecraft.updateReportEnvironment(ReportEnvironment.thirdParty(serverData != null ? serverData.ip : serverAddress.getHost()));
			minecraft.quickPlayLog().setWorldData(QuickPlayLog.Type.MULTIPLAYER, serverData.ip, serverData.name);
			minecraft.setScreen(connectScreen);
			connectScreen.connect(minecraft, serverAddress, serverData);
		}
	}

	private void connect(Minecraft minecraft, ServerAddress serverAddress, @Nullable ServerData serverData) {
		LOGGER.info("Connecting to {}, {}", serverAddress.getHost(), serverAddress.getPort());
		Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
			public void run() {
				InetSocketAddress inetSocketAddress = null;

				try {
					if (ConnectScreen.this.aborted) {
						return;
					}

					Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
					if (ConnectScreen.this.aborted) {
						return;
					}

					if (optional.isEmpty()) {
						minecraft.execute(
							() -> minecraft.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, ConnectScreen.this.connectFailedTitle, ConnectScreen.UNKNOWN_HOST_MESSAGE))
						);
						return;
					}

					inetSocketAddress = (InetSocketAddress)optional.get();
					Connection connection;
					synchronized (ConnectScreen.this) {
						if (ConnectScreen.this.aborted) {
							return;
						}

						connection = new Connection(PacketFlow.CLIENTBOUND);
						connection.setBandwidthLogger(minecraft.getDebugOverlay().getBandwidthLogger());
						ConnectScreen.this.channelFuture = Connection.connect(inetSocketAddress, minecraft.options.useNativeTransport(), connection);
					}

					ConnectScreen.this.channelFuture.syncUninterruptibly();
					synchronized (ConnectScreen.this) {
						if (ConnectScreen.this.aborted) {
							connection.disconnect(ConnectScreen.ABORT_CONNECTION);
							return;
						}

						ConnectScreen.this.connection = connection;
						minecraft.getDownloadedPackSource()
							.configureForServerControl(
								connection, serverData != null ? convertPackStatus(serverData.getResourcePackStatus()) : ServerPackManager.PackPromptStatus.PENDING
							);
					}

					ConnectScreen.this.connection
						.initiateServerboundPlayConnection(
							inetSocketAddress.getHostName(),
							inetSocketAddress.getPort(),
							new ClientHandshakePacketListenerImpl(
								ConnectScreen.this.connection, minecraft, serverData, ConnectScreen.this.parent, false, null, ConnectScreen.this::updateStatus
							)
						);
					ConnectScreen.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getUser().getProfileId()));
				} catch (Exception var9) {
					if (ConnectScreen.this.aborted) {
						return;
					}

					Exception exception3;
					if (var9.getCause() instanceof Exception exception2) {
						exception3 = exception2;
					} else {
						exception3 = var9;
					}

					ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var9);
					String string = inetSocketAddress == null
						? exception3.getMessage()
						: exception3.getMessage()
							.replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "")
							.replaceAll(inetSocketAddress.toString(), "");
					minecraft.execute(
						() -> minecraft.setScreen(
								new DisconnectedScreen(ConnectScreen.this.parent, ConnectScreen.this.connectFailedTitle, Component.translatable("disconnect.genericReason", string))
							)
					);
				}
			}

			private static ServerPackManager.PackPromptStatus convertPackStatus(ServerData.ServerPackStatus serverPackStatus) {
				return switch (serverPackStatus) {
					case ENABLED -> ServerPackManager.PackPromptStatus.ALLOWED;
					case DISABLED -> ServerPackManager.PackPromptStatus.DECLINED;
					case PROMPT -> ServerPackManager.PackPromptStatus.PENDING;
				};
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
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
			synchronized (this) {
				this.aborted = true;
				if (this.channelFuture != null) {
					this.channelFuture.cancel(true);
					this.channelFuture = null;
				}

				if (this.connection != null) {
					this.connection.disconnect(ABORT_CONNECTION);
				}
			}

			this.minecraft.setScreen(this.parent);
		}).bounds(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		long l = Util.getMillis();
		if (l - this.lastNarration > 2000L) {
			this.lastNarration = l;
			this.minecraft.getNarrator().sayNow(Component.translatable("narrator.joining"));
		}

		guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
	}
}
