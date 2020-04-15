/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.rcon.thread;

import com.google.common.collect.Lists;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RconThread
extends GenericThread {
    private static final Logger LOGGER = LogManager.getLogger();
    private final int port;
    private String serverIp;
    private ServerSocket socket;
    private final String rconPassword;
    private final List<RconClient> clients = Lists.newArrayList();
    private final ServerInterface serverInterface;

    public RconThread(ServerInterface serverInterface) {
        super("RCON Listener");
        this.serverInterface = serverInterface;
        DedicatedServerProperties dedicatedServerProperties = serverInterface.getProperties();
        this.port = dedicatedServerProperties.rconPort;
        this.rconPassword = dedicatedServerProperties.rconPassword;
        this.serverIp = serverInterface.getServerIp();
        if (this.serverIp.isEmpty()) {
            this.serverIp = "0.0.0.0";
        }
    }

    private void clearClients() {
        this.clients.removeIf(rconClient -> !rconClient.isRunning());
    }

    @Override
    public void run() {
        LOGGER.info("RCON running on {}:{}", (Object)this.serverIp, (Object)this.port);
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
                    LOGGER.info("IO exception: ", (Throwable)iOException);
                }
            }
        } finally {
            this.closeSocket(this.socket);
        }
    }

    @Override
    public void start() {
        if (this.rconPassword.isEmpty()) {
            LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
            return;
        }
        if (0 >= this.port || 65535 < this.port) {
            LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", (Object)this.port);
            return;
        }
        if (this.running) {
            return;
        }
        try {
            this.socket = new ServerSocket(this.port, 0, InetAddress.getByName(this.serverIp));
            this.socket.setSoTimeout(500);
            super.start();
        } catch (IOException iOException) {
            LOGGER.warn("Unable to initialise rcon on {}:{}", (Object)this.serverIp, (Object)this.port, (Object)iOException);
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
            LOGGER.warn("Failed to close socket", (Throwable)iOException);
        }
    }
}

