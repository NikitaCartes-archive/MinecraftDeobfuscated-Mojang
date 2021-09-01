package net.minecraft.server.chase;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChaseServer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final int serverPort;
	private final PlayerList playerList;
	private final int broadcastIntervalMs;
	private boolean wantsToRun = false;
	private ServerSocket serverSocket;

	public ChaseServer(int i, PlayerList playerList, int j) {
		this.serverPort = i;
		this.playerList = playerList;
		this.broadcastIntervalMs = j;
	}

	public void start() throws IOException {
		if (this.serverSocket != null && !this.serverSocket.isClosed()) {
			LOGGER.warn("Remote control server was asked to start, but it is already running. Will ignore.");
		} else {
			this.wantsToRun = true;
			this.serverSocket = new ServerSocket(this.serverPort);
			new Thread(this::runServer).start();
		}
	}

	public void stop() {
		this.wantsToRun = false;
		if (this.serverSocket != null) {
			try {
				this.serverSocket.close();
			} catch (IOException var2) {
				LOGGER.error("Failed to close remote control server socket", (Throwable)var2);
			}

			this.serverSocket = null;
		}
	}

	public void runServer() {
		try {
			while (this.wantsToRun) {
				LOGGER.info("Remote control server is listening for connections on port " + this.serverPort);
				Socket socket = this.serverSocket.accept();
				LOGGER.info("Remote control server received client connection on port " + socket.getPort());
				new Thread(() -> this.runClientHandler(socket)).start();
			}
		} catch (ClosedByInterruptException var12) {
			if (this.wantsToRun) {
				LOGGER.info("Remote control server closed by interrupt");
			}
		} catch (IOException var13) {
			if (this.wantsToRun) {
				LOGGER.error("Remote control server closed because of an IO exception", (Throwable)var13);
			}
		} finally {
			if (this.serverSocket != null && !this.serverSocket.isClosed()) {
				try {
					this.serverSocket.close();
				} catch (IOException var11) {
					LOGGER.warn("Failed to close remote control server socket", (Throwable)var11);
				}
			}
		}

		LOGGER.info("Remote control server is now stopped");
		this.wantsToRun = false;
	}

	private void runClientHandler(Socket socket) {
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

			while (this.wantsToRun) {
				Thread.sleep((long)this.broadcastIntervalMs);
				this.sendPlayerPosition(dataOutputStream);
			}
		} catch (InterruptedException var13) {
			LOGGER.info("Remote control client broadcast socket was interrupted and will be closed");
		} catch (IOException var14) {
			LOGGER.info("Remote control client broadcast socket got an IO exception and will be closed", (Throwable)var14);
		} finally {
			try {
				if (!socket.isClosed()) {
					socket.close();
				}
			} catch (IOException var12) {
				LOGGER.warn("Failed to close remote control client socket", (Throwable)var12);
			}
		}

		LOGGER.info("Closed connection to remote control client");
	}

	private void sendPlayerPosition(DataOutputStream dataOutputStream) throws IOException {
		List<ServerPlayer> list = this.playerList.getPlayers();
		if (!list.isEmpty()) {
			ServerPlayer serverPlayer = (ServerPlayer)list.get(0);
			String string = String.format(
				Locale.ROOT,
				"/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
				serverPlayer.level.dimension().location(),
				serverPlayer.getX(),
				serverPlayer.getY(),
				serverPlayer.getZ(),
				serverPlayer.getYRot(),
				serverPlayer.getXRot()
			);
			dataOutputStream.writeUTF(string);
		}
	}
}
