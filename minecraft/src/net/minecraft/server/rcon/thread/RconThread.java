package net.minecraft.server.rcon.thread;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RconThread extends GenericThread {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ServerSocket socket;
	private final String rconPassword;
	private final List<RconClient> clients = Lists.<RconClient>newArrayList();
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

	public void run() {
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

	@Nullable
	public static RconThread create(ServerInterface serverInterface) {
		DedicatedServerProperties dedicatedServerProperties = serverInterface.getProperties();
		String string = serverInterface.getServerIp();
		if (string.isEmpty()) {
			string = "0.0.0.0";
		}

		int i = dedicatedServerProperties.rconPort;
		if (0 < i && 65535 >= i) {
			String string2 = dedicatedServerProperties.rconPassword;
			if (string2.isEmpty()) {
				LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
				return null;
			} else {
				try {
					ServerSocket serverSocket = new ServerSocket(i, 0, InetAddress.getByName(string));
					serverSocket.setSoTimeout(500);
					RconThread rconThread = new RconThread(serverInterface, serverSocket, string2);
					if (!rconThread.start()) {
						return null;
					} else {
						LOGGER.info("RCON running on {}:{}", string, i);
						return rconThread;
					}
				} catch (IOException var7) {
					LOGGER.warn("Unable to initialise RCON on {}:{}", string, i, var7);
					return null;
				}
			}
		} else {
			LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", i);
			return null;
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
