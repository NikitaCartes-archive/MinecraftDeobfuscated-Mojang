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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RconThread extends GenericThread {
	private static final Logger LOGGER = LogManager.getLogger();
	private final int port;
	private String serverIp;
	private ServerSocket socket;
	private final String rconPassword;
	private final List<RconClient> clients = Lists.<RconClient>newArrayList();
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

	public void run() {
		LOGGER.info("RCON running on {}:{}", this.serverIp, this.port);

		try {
			while (this.running) {
				try {
					Socket socket = this.socket.accept();
					RconClient rconClient = new RconClient(this.serverInterface, this.rconPassword, socket);
					rconClient.start();
					this.clients.add(rconClient);
					this.clearClients();
				} catch (SocketTimeoutException var7) {
					this.clearClients();
				} catch (IOException var8) {
					if (this.running) {
						LOGGER.info("IO exception: ", (Throwable)var8);
					}
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
		} else if (0 >= this.port || 65535 < this.port) {
			LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", this.port);
		} else if (!this.running) {
			try {
				this.socket = new ServerSocket(this.port, 0, InetAddress.getByName(this.serverIp));
				this.socket.setSoTimeout(500);
				super.start();
			} catch (IOException var2) {
				LOGGER.warn("Unable to initialise rcon on {}:{}", this.serverIp, this.port, var2);
			}
		}
	}

	@Override
	public void stop() {
		this.running = false;
		this.closeSocket(this.socket);
		super.stop();

		for (RconClient rconClient : this.clients) {
			if (rconClient.isRunning()) {
				rconClient.stop();
			}
		}

		this.clients.clear();
	}

	private void closeSocket(ServerSocket serverSocket) {
		LOGGER.debug("closeSocket: {}", serverSocket);

		try {
			serverSocket.close();
		} catch (IOException var3) {
			LOGGER.warn("Failed to close socket", (Throwable)var3);
		}
	}
}
