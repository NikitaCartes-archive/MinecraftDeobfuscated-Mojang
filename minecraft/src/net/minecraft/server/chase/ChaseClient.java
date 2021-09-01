package net.minecraft.server.chase;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChaseClient {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int RECONNECT_INTERVAL_SECONDS = 5;
	private final String serverHost;
	private final int serverPort;
	private final MinecraftServer server;
	private boolean wantsToRun;
	private Socket socket;
	private Thread thread;

	public ChaseClient(String string, int i, MinecraftServer minecraftServer) {
		this.serverHost = string;
		this.serverPort = i;
		this.server = minecraftServer;
	}

	public void start() {
		if (this.thread != null && this.thread.isAlive()) {
			LOGGER.warn("Remote control client was asked to start, but it is already running. Will ignore.");
		}

		this.wantsToRun = true;
		this.thread = new Thread(this::run);
		this.thread.start();
	}

	public void stop() {
		this.wantsToRun = false;
		if (this.socket != null && !this.socket.isClosed()) {
			try {
				this.socket.close();
			} catch (IOException var2) {
				LOGGER.warn("Failed to close socket to remote control server", (Throwable)var2);
			}
		}

		this.socket = null;
		this.thread = null;
	}

	public void run() {
		String string = this.serverHost + ":" + this.serverPort;

		while (this.wantsToRun) {
			try {
				LOGGER.info("Connecting to remote control server " + string);
				this.socket = new Socket(this.serverHost, this.serverPort);
				LOGGER.info("Connected to remote control server! Will continuously execute the command broadcasted by that server.");

				try {
					DataInputStream dataInputStream = new DataInputStream(this.socket.getInputStream());

					while (this.wantsToRun) {
						String string2 = dataInputStream.readUTF();
						this.executeCommand(string2);
					}
				} catch (IOException var5) {
					LOGGER.warn("Lost connection to remote control server " + string + ". Will retry in 5s.");
				}
			} catch (IOException var6) {
				LOGGER.warn("Failed to connect to remote control server " + string + ". Will retry in 5s.");
			}

			if (this.wantsToRun) {
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException var4) {
				}
			}
		}
	}

	private void executeCommand(String string) {
		List<ServerPlayer> list = this.server.getPlayerList().getPlayers();
		if (!list.isEmpty()) {
			ServerPlayer serverPlayer = (ServerPlayer)list.get(0);
			ServerLevel serverLevel = this.server.overworld();
			CommandSourceStack commandSourceStack = new CommandSourceStack(
				serverPlayer, Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "", TextComponent.EMPTY, this.server, serverPlayer
			);
			Commands commands = this.server.getCommands();
			commands.performCommand(commandSourceStack, string);
		}
	}
}
