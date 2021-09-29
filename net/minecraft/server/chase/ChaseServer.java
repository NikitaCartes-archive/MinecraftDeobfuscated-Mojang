/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.chase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ChaseServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String serverBindAddress;
    private final int serverPort;
    private final PlayerList playerList;
    private final int broadcastIntervalMs;
    private volatile boolean wantsToRun;
    @Nullable
    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<Socket> clientSockets = new CopyOnWriteArrayList();

    public ChaseServer(String string, int i, PlayerList playerList, int j) {
        this.serverBindAddress = string;
        this.serverPort = i;
        this.playerList = playerList;
        this.broadcastIntervalMs = j;
    }

    public void start() throws IOException {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            LOGGER.warn("Remote control server was asked to start, but it is already running. Will ignore.");
            return;
        }
        this.wantsToRun = true;
        this.serverSocket = new ServerSocket(this.serverPort, 50, InetAddress.getByName(this.serverBindAddress));
        Thread thread = new Thread(this::runAcceptor, "chase-server-acceptor");
        thread.setDaemon(true);
        thread.start();
        Thread thread2 = new Thread(this::runSender, "chase-server-sender");
        thread2.setDaemon(true);
        thread2.start();
    }

    private void runSender() {
        PlayerPosition playerPosition = null;
        while (this.wantsToRun) {
            if (!this.clientSockets.isEmpty()) {
                PlayerPosition playerPosition2 = this.getPlayerPosition();
                if (playerPosition2 != null && !playerPosition2.equals(playerPosition)) {
                    playerPosition = playerPosition2;
                    byte[] bs = playerPosition2.format().getBytes(StandardCharsets.US_ASCII);
                    for (Socket socket : this.clientSockets) {
                        if (socket.isClosed()) continue;
                        Util.ioPool().submit(() -> {
                            try {
                                OutputStream outputStream = socket.getOutputStream();
                                outputStream.write(bs);
                                outputStream.flush();
                            } catch (IOException iOException) {
                                LOGGER.info("Remote control client socket got an IO exception and will be closed", (Throwable)iOException);
                                IOUtils.closeQuietly(socket);
                            }
                        });
                    }
                }
                List list = this.clientSockets.stream().filter(Socket::isClosed).collect(Collectors.toList());
                this.clientSockets.removeAll(list);
            }
            if (!this.wantsToRun) continue;
            try {
                Thread.sleep(this.broadcastIntervalMs);
            } catch (InterruptedException interruptedException) {}
        }
    }

    public void stop() {
        this.wantsToRun = false;
        IOUtils.closeQuietly(this.serverSocket);
        this.serverSocket = null;
    }

    private void runAcceptor() {
        try {
            while (this.wantsToRun) {
                if (this.serverSocket == null) continue;
                LOGGER.info("Remote control server is listening for connections on port {}", (Object)this.serverPort);
                Socket socket = this.serverSocket.accept();
                LOGGER.info("Remote control server received client connection on port {}", (Object)socket.getPort());
                this.clientSockets.add(socket);
            }
        } catch (ClosedByInterruptException closedByInterruptException) {
            if (this.wantsToRun) {
                LOGGER.info("Remote control server closed by interrupt");
            }
        } catch (IOException iOException) {
            if (this.wantsToRun) {
                LOGGER.error("Remote control server closed because of an IO exception", (Throwable)iOException);
            }
        } finally {
            IOUtils.closeQuietly(this.serverSocket);
        }
        LOGGER.info("Remote control server is now stopped");
        this.wantsToRun = false;
    }

    @Nullable
    private PlayerPosition getPlayerPosition() {
        List<ServerPlayer> list = this.playerList.getPlayers();
        if (list.isEmpty()) {
            return null;
        }
        ServerPlayer serverPlayer = list.get(0);
        String string = (String)ChaseCommand.DIMENSION_NAMES.inverse().get(serverPlayer.getLevel().dimension());
        if (string == null) {
            return null;
        }
        return new PlayerPosition(string, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
    }

    record PlayerPosition(String dimensionName, double x, double y, double z, float yRot, float xRot) {
        String format() {
            return String.format(Locale.ROOT, "t %s %.2f %.2f %.2f %.2f %.2f\n", this.dimensionName, this.x, this.y, this.z, Float.valueOf(this.yRot), Float.valueOf(this.xRot));
        }
    }
}

