/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.rcon.thread;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.rcon.thread.GenericThread;
import net.minecraft.server.rcon.thread.RconClient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RconThread
extends GenericThread {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerSocket socket;
    private final String rconPassword;
    private final List<RconClient> clients = Lists.newArrayList();
    private final ServerInterface serverInterface;

    private RconThread(ServerInterface serverInterface, ServerSocket serverSocket, String string) {
        super("RCON Listener");
        this.serverInterface = serverInterface;
        this.socket = serverSocket;
        this.rconPassword = string;
    }

    private void clearClients() {
        this.clients.removeIf(rconClient -> !rconClient.isRunning());
    }

    @Override
    public void run() {
        try {
            while (this.running) {
                try {
                    Socket socket = this.socket.accept();
                    RconClient rconClient = new RconClient(this.serverInterface, this.rconPassword, socket);
                    rconClient.start();
                    this.clients.add(rconClient);
                    this.clearClients();
                } catch (SocketTimeoutException socketTimeoutException) {
                    this.clearClients();
                } catch (IOException iOException) {
                    if (!this.running) continue;
                    LOGGER.info("IO exception: ", iOException);
                }
            }
        } finally {
            this.closeSocket(this.socket);
        }
    }

    @Nullable
    public static RconThread create(ServerInterface serverInterface) {
        int i;
        DedicatedServerProperties dedicatedServerProperties = serverInterface.getProperties();
        String string = serverInterface.getServerIp();
        if (string.isEmpty()) {
            string = "0.0.0.0";
        }
        if (0 >= (i = dedicatedServerProperties.rconPort) || 65535 < i) {
            LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", (Object)i);
            return null;
        }
        String string2 = dedicatedServerProperties.rconPassword;
        if (string2.isEmpty()) {
            LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
            return null;
        }
        try {
            ServerSocket serverSocket = new ServerSocket(i, 0, InetAddress.getByName(string));
            serverSocket.setSoTimeout(500);
            RconThread rconThread = new RconThread(serverInterface, serverSocket, string2);
            if (!rconThread.start()) {
                return null;
            }
            LOGGER.info("RCON running on {}:{}", (Object)string, (Object)i);
            return rconThread;
        } catch (IOException iOException) {
            LOGGER.warn("Unable to initialise RCON on {}:{}", string, i, iOException);
            return null;
        }
    }

    @Override
    public void stop() {
        this.running = false;
        this.closeSocket(this.socket);
        super.stop();
        for (RconClient rconClient : this.clients) {
            if (!rconClient.isRunning()) continue;
            rconClient.stop();
        }
        this.clients.clear();
    }

    private void closeSocket(ServerSocket serverSocket) {
        LOGGER.debug("closeSocket: {}", (Object)serverSocket);
        try {
            serverSocket.close();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to close socket", iOException);
        }
    }
}

