package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ConnectScreen extends Screen {
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	static final Logger LOGGER = LogUtils.getLogger();
	private static final long NARRATION_DELAY_MS = 2000L;
	public static final Component UNKNOWN_HOST_MESSAGE = Component.translatable("disconnect.genericReason", Component.translatable("disconnect.unknownHost"));
	@Nullable
	volatile Connection connection;
	volatile boolean aborted;
	final Screen parent;
	private Component status = Component.translatable("connect.connecting");
	private long lastNarration = -1L;

	private ConnectScreen(Screen screen) {
		super(GameNarrator.NO_TITLE);
		this.parent = screen;
	}

	public static void startConnecting(Screen screen, Minecraft minecraft, ServerAddress serverAddress, @Nullable ServerData serverData) {
		ConnectScreen connectScreen = new ConnectScreen(screen);
		minecraft.clearLevel();
		minecraft.prepareForMultiplayer();
		minecraft.setCurrentServer(serverData);
		minecraft.setScreen(connectScreen);
		connectScreen.connect(minecraft, serverAddress);
	}

	private void connect(Minecraft minecraft, ServerAddress serverAddress) {
		final CompletableFuture<Optional<ProfilePublicKey.Data>> completableFuture = minecraft.getProfileKeyPairManager().preparePublicKey();
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

					if (!optional.isPresent()) {
						minecraft.execute(
							() -> minecraft.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, ConnectScreen.UNKNOWN_HOST_MESSAGE))
						);
						return;
					}

					inetSocketAddress = (InetSocketAddress)optional.get();
					ConnectScreen.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
					ConnectScreen.this.connection
						.setListener(new ClientHandshakePacketListenerImpl(ConnectScreen.this.connection, minecraft, ConnectScreen.this.parent, ConnectScreen.this::updateStatus));
					ConnectScreen.this.connection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
					ConnectScreen.this.connection
						.send(
							new ServerboundHelloPacket(
								minecraft.getUser().getName(), (Optional<ProfilePublicKey.Data>)completableFuture.join(), Optional.ofNullable(minecraft.getUser().getProfileId())
							)
						);
				} catch (Exception var6) {
					if (ConnectScreen.this.aborted) {
						return;
					}

					Exception exception3;
					if (var6.getCause() instanceof Exception exception2) {
						exception3 = exception2;
					} else {
						exception3 = var6;
					}

					ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var6);
					String string = inetSocketAddress == null
						? exception3.getMessage()
						: exception3.getMessage()
							.replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "")
							.replaceAll(inetSocketAddress.toString(), "");
					minecraft.execute(
						() -> minecraft.setScreen(
								new DisconnectedScreen(ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", string))
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
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, button -> {
			this.aborted = true;
			if (this.connection != null) {
				this.connection.disconnect(Component.translatable("connect.aborted"));
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
			this.minecraft.getNarrator().sayNow(Component.translatable("narrator.joining"));
		}

		drawCenteredString(poseStack, this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
		super.render(poseStack, i, j, f);
	}
}
