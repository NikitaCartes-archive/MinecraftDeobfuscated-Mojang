package net.minecraft.client.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LanServerDetection {
	static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	static final Logger LOGGER = LogManager.getLogger();

	@Environment(EnvType.CLIENT)
	public static class LanServerDetector extends Thread {
		private final LanServerDetection.LanServerList serverList;
		private final InetAddress pingGroup;
		private final MulticastSocket socket;

		public LanServerDetector(LanServerDetection.LanServerList lanServerList) throws IOException {
			super("LanServerDetector #" + LanServerDetection.UNIQUE_THREAD_ID.incrementAndGet());
			this.serverList = lanServerList;
			this.setDaemon(true);
			this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LanServerDetection.LOGGER));
			this.socket = new MulticastSocket(4445);
			this.pingGroup = InetAddress.getByName("224.0.2.60");
			this.socket.setSoTimeout(5000);
			this.socket.joinGroup(this.pingGroup);
		}

		public void run() {
			byte[] bs = new byte[1024];

			while (!this.isInterrupted()) {
				DatagramPacket datagramPacket = new DatagramPacket(bs, bs.length);

				try {
					this.socket.receive(datagramPacket);
				} catch (SocketTimeoutException var5) {
					continue;
				} catch (IOException var6) {
					LanServerDetection.LOGGER.error("Couldn't ping server", (Throwable)var6);
					break;
				}

				String string = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);
				LanServerDetection.LOGGER.debug("{}: {}", datagramPacket.getAddress(), string);
				this.serverList.addServer(string, datagramPacket.getAddress());
			}

			try {
				this.socket.leaveGroup(this.pingGroup);
			} catch (IOException var4) {
			}

			this.socket.close();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LanServerList {
		private final List<LanServer> servers = Lists.<LanServer>newArrayList();
		private boolean isDirty;

		public synchronized boolean isDirty() {
			return this.isDirty;
		}

		public synchronized void markClean() {
			this.isDirty = false;
		}

		public synchronized List<LanServer> getServers() {
			return Collections.unmodifiableList(this.servers);
		}

		public synchronized void addServer(String string, InetAddress inetAddress) {
			String string2 = LanServerPinger.parseMotd(string);
			String string3 = LanServerPinger.parseAddress(string);
			if (string3 != null) {
				string3 = inetAddress.getHostAddress() + ":" + string3;
				boolean bl = false;

				for (LanServer lanServer : this.servers) {
					if (lanServer.getAddress().equals(string3)) {
						lanServer.updatePingTime();
						bl = true;
						break;
					}
				}

				if (!bl) {
					this.servers.add(new LanServer(string2, string3));
					this.isDirty = true;
				}
			}
		}
	}
}
