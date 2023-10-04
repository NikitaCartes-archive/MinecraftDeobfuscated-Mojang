package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements ServerLoginPacketListener, TickablePacketListener {
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_TICKS_BEFORE_LOGIN = 600;
	private static final Component DISCONNECT_UNEXPECTED_QUERY = Component.translatable("multiplayer.disconnect.unexpected_query_response");
	private final byte[] challenge;
	final MinecraftServer server;
	final Connection connection;
	private volatile ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
	private int tick;
	@Nullable
	String requestedUsername;
	@Nullable
	private GameProfile authenticatedProfile;
	private final String serverId = "";

	public ServerLoginPacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
		this.server = minecraftServer;
		this.connection = connection;
		this.challenge = Ints.toByteArray(RandomSource.create().nextInt());
	}

	@Override
	public void tick() {
		if (this.state == ServerLoginPacketListenerImpl.State.VERIFYING) {
			this.verifyLoginAndFinishConnectionSetup((GameProfile)Objects.requireNonNull(this.authenticatedProfile));
		}

		if (this.state == ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT
			&& !this.isPlayerAlreadyInWorld((GameProfile)Objects.requireNonNull(this.authenticatedProfile))) {
			this.finishLoginAndWaitForClient(this.authenticatedProfile);
		}

		if (this.tick++ == 600) {
			this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
		}
	}

	@Override
	public boolean isAcceptingMessages() {
		return this.connection.isConnected();
	}

	public void disconnect(Component component) {
		try {
			LOGGER.info("Disconnecting {}: {}", this.getUserName(), component.getString());
			this.connection.send(new ClientboundLoginDisconnectPacket(component));
			this.connection.disconnect(component);
		} catch (Exception var3) {
			LOGGER.error("Error whilst disconnecting player", (Throwable)var3);
		}
	}

	private boolean isPlayerAlreadyInWorld(GameProfile gameProfile) {
		return this.server.getPlayerList().getPlayer(gameProfile.getId()) != null;
	}

	@Override
	public void onDisconnect(Component component) {
		LOGGER.info("{} lost connection: {}", this.getUserName(), component.getString());
	}

	public String getUserName() {
		String string = this.connection.getLoggableAddress(this.server.logIPs());
		return this.requestedUsername != null ? this.requestedUsername + " (" + string + ")" : string;
	}

	@Override
	public void handleHello(ServerboundHelloPacket serverboundHelloPacket) {
		Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
		Validate.validState(Player.isValidUsername(serverboundHelloPacket.name()), "Invalid characters in username");
		this.requestedUsername = serverboundHelloPacket.name();
		GameProfile gameProfile = this.server.getSingleplayerProfile();
		if (gameProfile != null && this.requestedUsername.equalsIgnoreCase(gameProfile.getName())) {
			this.startClientVerification(gameProfile);
		} else {
			if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
				this.state = ServerLoginPacketListenerImpl.State.KEY;
				this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge));
			} else {
				this.startClientVerification(UUIDUtil.createOfflineProfile(this.requestedUsername));
			}
		}
	}

	void startClientVerification(GameProfile gameProfile) {
		this.authenticatedProfile = gameProfile;
		this.state = ServerLoginPacketListenerImpl.State.VERIFYING;
	}

	private void verifyLoginAndFinishConnectionSetup(GameProfile gameProfile) {
		PlayerList playerList = this.server.getPlayerList();
		Component component = playerList.canPlayerLogin(this.connection.getRemoteAddress(), gameProfile);
		if (component != null) {
			this.disconnect(component);
		} else {
			if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
				this.connection
					.send(
						new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
						PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true))
					);
			}

			boolean bl = playerList.disconnectAllPlayersWithProfile(gameProfile);
			if (bl) {
				this.state = ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT;
			} else {
				this.finishLoginAndWaitForClient(gameProfile);
			}
		}
	}

	private void finishLoginAndWaitForClient(GameProfile gameProfile) {
		this.state = ServerLoginPacketListenerImpl.State.PROTOCOL_SWITCHING;
		this.connection.send(new ClientboundGameProfilePacket(gameProfile));
	}

	@Override
	public void handleKey(ServerboundKeyPacket serverboundKeyPacket) {
		Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

		final String string;
		try {
			PrivateKey privateKey = this.server.getKeyPair().getPrivate();
			if (!serverboundKeyPacket.isChallengeValid(this.challenge, privateKey)) {
				throw new IllegalStateException("Protocol error");
			}

			SecretKey secretKey = serverboundKeyPacket.getSecretKey(privateKey);
			Cipher cipher = Crypt.getCipher(2, secretKey);
			Cipher cipher2 = Crypt.getCipher(1, secretKey);
			string = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretKey)).toString(16);
			this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
			this.connection.setEncryptionKey(cipher, cipher2);
		} catch (CryptException var7) {
			throw new IllegalStateException("Protocol error", var7);
		}

		Thread thread = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
			public void run() {
				String string = (String)Objects.requireNonNull(ServerLoginPacketListenerImpl.this.requestedUsername, "Player name not initialized");

				try {
					ProfileResult profileResult = ServerLoginPacketListenerImpl.this.server.getSessionService().hasJoinedServer(string, string, this.getAddress());
					if (profileResult != null) {
						GameProfile gameProfile = profileResult.profile();
						ServerLoginPacketListenerImpl.LOGGER.info("UUID of player {} is {}", gameProfile.getName(), gameProfile.getId());
						ServerLoginPacketListenerImpl.this.startClientVerification(gameProfile);
					} else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
						ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
						ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile(string));
					} else {
						ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
						ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", string);
					}
				} catch (AuthenticationUnavailableException var4) {
					if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
						ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
						ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile(string));
					} else {
						ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
						ServerLoginPacketListenerImpl.LOGGER.error("Couldn't verify username because servers are unavailable");
					}
				}
			}

			@Nullable
			private InetAddress getAddress() {
				SocketAddress socketAddress = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
				return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && socketAddress instanceof InetSocketAddress
					? ((InetSocketAddress)socketAddress).getAddress()
					: null;
			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
	}

	@Override
	public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket serverboundCustomQueryAnswerPacket) {
		this.disconnect(DISCONNECT_UNEXPECTED_QUERY);
	}

	@Override
	public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundLoginAcknowledgedPacket) {
		Validate.validState(this.state == ServerLoginPacketListenerImpl.State.PROTOCOL_SWITCHING, "Unexpected login acknowledgement packet");
		CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial((GameProfile)Objects.requireNonNull(this.authenticatedProfile));
		ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = new ServerConfigurationPacketListenerImpl(
			this.server, this.connection, commonListenerCookie
		);
		this.connection.setListener(serverConfigurationPacketListenerImpl);
		serverConfigurationPacketListenerImpl.startConfiguration();
		this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
	}

	static enum State {
		HELLO,
		KEY,
		AUTHENTICATING,
		NEGOTIATING,
		VERIFYING,
		WAITING_FOR_DUPE_DISCONNECT,
		PROTOCOL_SWITCHING,
		ACCEPTED;
	}
}
