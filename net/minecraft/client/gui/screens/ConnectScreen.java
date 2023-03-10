/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ConnectScreen
extends Screen {
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

    public static void startConnecting(Screen screen, Minecraft minecraft, ServerAddress serverAddress, ServerData serverData) {
        ConnectScreen connectScreen = new ConnectScreen(screen);
        minecraft.clearLevel();
        minecraft.prepareForMultiplayer();
        minecraft.updateReportEnvironment(ReportEnvironment.thirdParty(serverData != null ? serverData.ip : serverAddress.getHost()));
        minecraft.setScreen(connectScreen);
        connectScreen.connect(minecraft, serverAddress, serverData);
    }

    private void connect(final Minecraft minecraft, final ServerAddress serverAddress, final @Nullable ServerData serverData) {
        LOGGER.info("Connecting to {}, {}", (Object)serverAddress.getHost(), (Object)serverAddress.getPort());
        Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()){

            @Override
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
                        minecraft.execute(() -> minecraft.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, UNKNOWN_HOST_MESSAGE)));
                        return;
                    }
                    inetSocketAddress = optional.get();
                    ConnectScreen.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
                    ConnectScreen.this.connection.setListener(new ClientHandshakePacketListenerImpl(ConnectScreen.this.connection, minecraft, serverData, ConnectScreen.this.parent, false, null, ConnectScreen.this::updateStatus));
                    ConnectScreen.this.connection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
                    ConnectScreen.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), Optional.ofNullable(minecraft.getUser().getProfileId())));
                } catch (Exception exception) {
                    Exception exception2;
                    if (ConnectScreen.this.aborted) {
                        return;
                    }
                    Throwable throwable = exception.getCause();
                    Exception exception3 = throwable instanceof Exception ? (exception2 = (Exception)throwable) : exception;
                    LOGGER.error("Couldn't connect to server", exception);
                    String string = inetSocketAddress == null ? exception3.getMessage() : exception3.getMessage().replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "").replaceAll(inetSocketAddress.toString(), "");
                    minecraft.execute(() -> minecraft.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", string))));
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
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.aborted = true;
            if (this.connection != null) {
                this.connection.disconnect(Component.translatable("connect.aborted"));
            }
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        long l = Util.getMillis();
        if (l - this.lastNarration > 2000L) {
            this.lastNarration = l;
            this.minecraft.getNarrator().sayNow(Component.translatable("narrator.joining"));
        }
        ConnectScreen.drawCenteredString(poseStack, this.font, this.status, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }
}

