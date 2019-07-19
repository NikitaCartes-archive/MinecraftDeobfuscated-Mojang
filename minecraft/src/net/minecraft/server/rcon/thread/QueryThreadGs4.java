package net.minecraft.server.rcon.thread;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.NetworkDataOutputStream;
import net.minecraft.server.rcon.PktUtils;

public class QueryThreadGs4 extends GenericThread {
	private long lastChallengeCheck;
	private final int port;
	private final int serverPort;
	private final int maxPlayers;
	private final String serverName;
	private final String worldName;
	private DatagramSocket socket;
	private final byte[] buffer = new byte[1460];
	private DatagramPacket request;
	private final Map<SocketAddress, String> idents;
	private String hostIp;
	private String serverIp;
	private final Map<SocketAddress, QueryThreadGs4.RequestChallenge> validChallenges;
	private final long lastChallengeClean;
	private final NetworkDataOutputStream rulesResponse;
	private long lastRulesResponse;

	public QueryThreadGs4(ServerInterface serverInterface) {
		super(serverInterface, "Query Listener");
		this.port = serverInterface.getProperties().queryPort;
		this.serverIp = serverInterface.getServerIp();
		this.serverPort = serverInterface.getServerPort();
		this.serverName = serverInterface.getServerName();
		this.maxPlayers = serverInterface.getMaxPlayers();
		this.worldName = serverInterface.getLevelIdName();
		this.lastRulesResponse = 0L;
		this.hostIp = "0.0.0.0";
		if (!this.serverIp.isEmpty() && !this.hostIp.equals(this.serverIp)) {
			this.hostIp = this.serverIp;
		} else {
			this.serverIp = "0.0.0.0";

			try {
				InetAddress inetAddress = InetAddress.getLocalHost();
				this.hostIp = inetAddress.getHostAddress();
			} catch (UnknownHostException var3) {
				this.warn("Unable to determine local host IP, please set server-ip in server.properties: " + var3.getMessage());
			}
		}

		this.idents = Maps.<SocketAddress, String>newHashMap();
		this.rulesResponse = new NetworkDataOutputStream(1460);
		this.validChallenges = Maps.<SocketAddress, QueryThreadGs4.RequestChallenge>newHashMap();
		this.lastChallengeClean = new Date().getTime();
	}

	private void sendTo(byte[] bs, DatagramPacket datagramPacket) throws IOException {
		this.socket.send(new DatagramPacket(bs, bs.length, datagramPacket.getSocketAddress()));
	}

	private boolean processPacket(DatagramPacket datagramPacket) throws IOException {
		byte[] bs = datagramPacket.getData();
		int i = datagramPacket.getLength();
		SocketAddress socketAddress = datagramPacket.getSocketAddress();
		this.debug("Packet len " + i + " [" + socketAddress + "]");
		if (3 <= i && -2 == bs[0] && -3 == bs[1]) {
			this.debug("Packet '" + PktUtils.toHexString(bs[2]) + "' [" + socketAddress + "]");
			switch (bs[2]) {
				case 0:
					if (!this.validChallenge(datagramPacket)) {
						this.debug("Invalid challenge [" + socketAddress + "]");
						return false;
					} else if (15 == i) {
						this.sendTo(this.buildRuleResponse(datagramPacket), datagramPacket);
						this.debug("Rules [" + socketAddress + "]");
					} else {
						NetworkDataOutputStream networkDataOutputStream = new NetworkDataOutputStream(1460);
						networkDataOutputStream.write(0);
						networkDataOutputStream.writeBytes(this.getIdentBytes(datagramPacket.getSocketAddress()));
						networkDataOutputStream.writeString(this.serverName);
						networkDataOutputStream.writeString("SMP");
						networkDataOutputStream.writeString(this.worldName);
						networkDataOutputStream.writeString(Integer.toString(this.currentPlayerCount()));
						networkDataOutputStream.writeString(Integer.toString(this.maxPlayers));
						networkDataOutputStream.writeShort((short)this.serverPort);
						networkDataOutputStream.writeString(this.hostIp);
						this.sendTo(networkDataOutputStream.toByteArray(), datagramPacket);
						this.debug("Status [" + socketAddress + "]");
					}
				default:
					return true;
				case 9:
					this.sendChallenge(datagramPacket);
					this.debug("Challenge [" + socketAddress + "]");
					return true;
			}
		} else {
			this.debug("Invalid packet [" + socketAddress + "]");
			return false;
		}
	}

	private byte[] buildRuleResponse(DatagramPacket datagramPacket) throws IOException {
		long l = Util.getMillis();
		if (l < this.lastRulesResponse + 5000L) {
			byte[] bs = this.rulesResponse.toByteArray();
			byte[] cs = this.getIdentBytes(datagramPacket.getSocketAddress());
			bs[1] = cs[0];
			bs[2] = cs[1];
			bs[3] = cs[2];
			bs[4] = cs[3];
			return bs;
		} else {
			this.lastRulesResponse = l;
			this.rulesResponse.reset();
			this.rulesResponse.write(0);
			this.rulesResponse.writeBytes(this.getIdentBytes(datagramPacket.getSocketAddress()));
			this.rulesResponse.writeString("splitnum");
			this.rulesResponse.write(128);
			this.rulesResponse.write(0);
			this.rulesResponse.writeString("hostname");
			this.rulesResponse.writeString(this.serverName);
			this.rulesResponse.writeString("gametype");
			this.rulesResponse.writeString("SMP");
			this.rulesResponse.writeString("game_id");
			this.rulesResponse.writeString("MINECRAFT");
			this.rulesResponse.writeString("version");
			this.rulesResponse.writeString(this.serverInterface.getServerVersion());
			this.rulesResponse.writeString("plugins");
			this.rulesResponse.writeString(this.serverInterface.getPluginNames());
			this.rulesResponse.writeString("map");
			this.rulesResponse.writeString(this.worldName);
			this.rulesResponse.writeString("numplayers");
			this.rulesResponse.writeString("" + this.currentPlayerCount());
			this.rulesResponse.writeString("maxplayers");
			this.rulesResponse.writeString("" + this.maxPlayers);
			this.rulesResponse.writeString("hostport");
			this.rulesResponse.writeString("" + this.serverPort);
			this.rulesResponse.writeString("hostip");
			this.rulesResponse.writeString(this.hostIp);
			this.rulesResponse.write(0);
			this.rulesResponse.write(1);
			this.rulesResponse.writeString("player_");
			this.rulesResponse.write(0);
			String[] strings = this.serverInterface.getPlayerNames();

			for (String string : strings) {
				this.rulesResponse.writeString(string);
			}

			this.rulesResponse.write(0);
			return this.rulesResponse.toByteArray();
		}
	}

	private byte[] getIdentBytes(SocketAddress socketAddress) {
		return ((QueryThreadGs4.RequestChallenge)this.validChallenges.get(socketAddress)).getIdentBytes();
	}

	private Boolean validChallenge(DatagramPacket datagramPacket) {
		SocketAddress socketAddress = datagramPacket.getSocketAddress();
		if (!this.validChallenges.containsKey(socketAddress)) {
			return false;
		} else {
			byte[] bs = datagramPacket.getData();
			return ((QueryThreadGs4.RequestChallenge)this.validChallenges.get(socketAddress)).getChallenge()
					!= PktUtils.intFromNetworkByteArray(bs, 7, datagramPacket.getLength())
				? false
				: true;
		}
	}

	private void sendChallenge(DatagramPacket datagramPacket) throws IOException {
		QueryThreadGs4.RequestChallenge requestChallenge = new QueryThreadGs4.RequestChallenge(datagramPacket);
		this.validChallenges.put(datagramPacket.getSocketAddress(), requestChallenge);
		this.sendTo(requestChallenge.getChallengeBytes(), datagramPacket);
	}

	private void pruneChallenges() {
		if (this.running) {
			long l = Util.getMillis();
			if (l >= this.lastChallengeCheck + 30000L) {
				this.lastChallengeCheck = l;
				Iterator<Entry<SocketAddress, QueryThreadGs4.RequestChallenge>> iterator = this.validChallenges.entrySet().iterator();

				while (iterator.hasNext()) {
					Entry<SocketAddress, QueryThreadGs4.RequestChallenge> entry = (Entry<SocketAddress, QueryThreadGs4.RequestChallenge>)iterator.next();
					if (((QueryThreadGs4.RequestChallenge)entry.getValue()).before(l)) {
						iterator.remove();
					}
				}
			}
		}
	}

	public void run() {
		this.info("Query running on " + this.serverIp + ":" + this.port);
		this.lastChallengeCheck = Util.getMillis();
		this.request = new DatagramPacket(this.buffer, this.buffer.length);

		try {
			while (this.running) {
				try {
					this.socket.receive(this.request);
					this.pruneChallenges();
					this.processPacket(this.request);
				} catch (SocketTimeoutException var7) {
					this.pruneChallenges();
				} catch (PortUnreachableException var8) {
				} catch (IOException var9) {
					this.recoverSocketError(var9);
				}
			}
		} finally {
			this.closeSockets();
		}
	}

	@Override
	public void start() {
		if (!this.running) {
			if (0 < this.port && 65535 >= this.port) {
				if (this.initSocket()) {
					super.start();
				}
			} else {
				this.warn("Invalid query port " + this.port + " found in server.properties (queries disabled)");
			}
		}
	}

	private void recoverSocketError(Exception exception) {
		if (this.running) {
			this.warn("Unexpected exception, buggy JRE? (" + exception + ")");
			if (!this.initSocket()) {
				this.error("Failed to recover from buggy JRE, shutting down!");
				this.running = false;
			}
		}
	}

	private boolean initSocket() {
		try {
			this.socket = new DatagramSocket(this.port, InetAddress.getByName(this.serverIp));
			this.registerSocket(this.socket);
			this.socket.setSoTimeout(500);
			return true;
		} catch (SocketException var2) {
			this.warn("Unable to initialise query system on " + this.serverIp + ":" + this.port + " (Socket): " + var2.getMessage());
		} catch (UnknownHostException var3) {
			this.warn("Unable to initialise query system on " + this.serverIp + ":" + this.port + " (Unknown Host): " + var3.getMessage());
		} catch (Exception var4) {
			this.warn("Unable to initialise query system on " + this.serverIp + ":" + this.port + " (E): " + var4.getMessage());
		}

		return false;
	}

	class RequestChallenge {
		private final long time = new Date().getTime();
		private final int challenge;
		private final byte[] identBytes;
		private final byte[] challengeBytes;
		private final String ident;

		public RequestChallenge(DatagramPacket datagramPacket) {
			byte[] bs = datagramPacket.getData();
			this.identBytes = new byte[4];
			this.identBytes[0] = bs[3];
			this.identBytes[1] = bs[4];
			this.identBytes[2] = bs[5];
			this.identBytes[3] = bs[6];
			this.ident = new String(this.identBytes, StandardCharsets.UTF_8);
			this.challenge = new Random().nextInt(16777216);
			this.challengeBytes = String.format("\t%s%d\u0000", this.ident, this.challenge).getBytes(StandardCharsets.UTF_8);
		}

		public Boolean before(long l) {
			return this.time < l;
		}

		public int getChallenge() {
			return this.challenge;
		}

		public byte[] getChallengeBytes() {
			return this.challengeBytes;
		}

		public byte[] getIdentBytes() {
			return this.identBytes;
		}
	}
}
