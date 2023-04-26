package net.minecraft.server.chase;

import com.mojang.logging.LogUtils;
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
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ChaseServer {
	private static final Logger LOGGER = LogUtils.getLogger();
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
		} else {
			this.wantsToRun = true;
			this.serverSocket = new ServerSocket(this.serverPort, 50, InetAddress.getByName(this.serverBindAddress));
			Thread thread = new Thread(this::runAcceptor, "chase-server-acceptor");
			thread.setDaemon(true);
			thread.start();
			Thread thread2 = new Thread(this::runSender, "chase-server-sender");
			thread2.setDaemon(true);
			thread2.start();
		}
	}

	private void runSender() {
		ChaseServer.PlayerPosition playerPosition = null;

		while (this.wantsToRun) {
			if (!this.clientSockets.isEmpty()) {
				ChaseServer.PlayerPosition playerPosition2 = this.getPlayerPosition();
				if (playerPosition2 != null && !playerPosition2.equals(playerPosition)) {
					playerPosition = playerPosition2;
					byte[] bs = playerPosition2.format().getBytes(StandardCharsets.US_ASCII);

					for (Socket socket : this.clientSockets) {
						if (!socket.isClosed()) {
							Util.ioPool().submit(() -> {
								try {
									OutputStream outputStream = socket.getOutputStream();
									outputStream.write(bs);
									outputStream.flush();
								} catch (IOException var3x) {
									LOGGER.info("Remote control client socket got an IO exception and will be closed", (Throwable)var3x);
									IOUtils.closeQuietly(socket);
								}
							});
						}
					}
				}

				List<Socket> list = (List<Socket>)this.clientSockets.stream().filter(Socket::isClosed).collect(Collectors.toList());
				this.clientSockets.removeAll(list);
			}

			if (this.wantsToRun) {
				try {
					Thread.sleep((long)this.broadcastIntervalMs);
				} catch (InterruptedException var6) {
				}
			}
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
				if (this.serverSocket != null) {
					LOGGER.info("Remote control server is listening for connections on port {}", this.serverPort);
					Socket socket = this.serverSocket.accept();
					LOGGER.info("Remote control server received client connection on port {}", socket.getPort());
					this.clientSockets.add(socket);
				}
			}
		} catch (ClosedByInterruptException var6) {
			if (this.wantsToRun) {
				LOGGER.info("Remote control server closed by interrupt");
			}
		} catch (IOException var7) {
			if (this.wantsToRun) {
				LOGGER.error("Remote control server closed because of an IO exception", (Throwable)var7);
			}
		} finally {
			IOUtils.closeQuietly(this.serverSocket);
		}

		LOGGER.info("Remote control server is now stopped");
		this.wantsToRun = false;
	}

	@Nullable
	private ChaseServer.PlayerPosition getPlayerPosition() {
		List<ServerPlayer> list = this.playerList.getPlayers();
		if (list.isEmpty()) {
			return null;
		} else {
			ServerPlayer serverPlayer = (ServerPlayer)list.get(0);
			String string = (String)ChaseCommand.DIMENSION_NAMES.inverse().get(serverPlayer.level().dimension());
			return string == null
				? null
				: new ChaseServer.PlayerPosition(string, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
		}
	}

	static record PlayerPosition(String dimensionName, double x, double y, double z, float yRot, float xRot) {
		String format() {
			return String.format(Locale.ROOT, "t %s %.2f %.2f %.2f %.2f %.2f\n", this.dimensionName, this.x, this.y, this.z, this.yRot, this.xRot);
		}
	}
}
