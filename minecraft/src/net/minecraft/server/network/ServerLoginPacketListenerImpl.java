package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements ServerLoginPacketListener {
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_TICKS_BEFORE_LOGIN = 600;
	private static final Random RANDOM = new Random();
	private final byte[] nonce = new byte[4];
	final MinecraftServer server;
	public final Connection connection;
	ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
	private int tick;
	@Nullable
	GameProfile gameProfile;
	private final String serverId = "";
	@Nullable
	private ServerPlayer delayedAcceptPlayer;

	public ServerLoginPacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
		this.server = minecraftServer;
		this.connection = connection;
		RANDOM.nextBytes(this.nonce);
	}

	public void tick() {
		if (this.state == ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT) {
			this.handleAcceptedLogin();
		} else if (this.state == ServerLoginPacketListenerImpl.State.DELAY_ACCEPT) {
			ServerPlayer serverPlayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
			if (serverPlayer == null) {
				this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
				this.placeNewPlayer(this.delayedAcceptPlayer);
				this.delayedAcceptPlayer = null;
			}
		}

		if (this.tick++ == 600) {
			this.disconnect(new TranslatableComponent("multiplayer.disconnect.slow_login"));
		}
	}

	@Override
	public Connection getConnection() {
		return this.connection;
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

	public void handleAcceptedLogin() {
		if (!this.gameProfile.isComplete()) {
			this.gameProfile = this.createFakeProfile(this.gameProfile);
		}

		Component component = this.server.getPlayerList().canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
		if (component != null) {
			this.disconnect(component);
		} else {
			this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
			if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
				this.connection
					.send(
						new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
						channelFuture -> this.connection.setupCompression(this.server.getCompressionThreshold(), true)
					);
			}

			this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
			ServerPlayer serverPlayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());

			try {
				ServerPlayer serverPlayer2 = this.server.getPlayerList().getPlayerForLogin(this.gameProfile);
				if (serverPlayer != null) {
					this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
					this.delayedAcceptPlayer = serverPlayer2;
				} else {
					this.placeNewPlayer(serverPlayer2);
				}
			} catch (Exception var5) {
				LOGGER.error("Couldn't place player in world", (Throwable)var5);
				Component component2 = new TranslatableComponent("multiplayer.disconnect.invalid_player_data");
				this.connection.send(new ClientboundDisconnectPacket(component2));
				this.connection.disconnect(component2);
			}
		}
	}

	private void placeNewPlayer(ServerPlayer serverPlayer) {
		this.server.getPlayerList().placeNewPlayer(this.connection, serverPlayer);
	}

	@Override
	public void onDisconnect(Component component) {
		LOGGER.info("{} lost connection: {}", this.getUserName(), component.getString());
	}

	public String getUserName() {
		return this.gameProfile != null ? this.gameProfile + " (" + this.connection.getRemoteAddress() + ")" : String.valueOf(this.connection.getRemoteAddress());
	}

	@Override
	public void handleHello(ServerboundHelloPacket serverboundHelloPacket) {
		Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
		this.gameProfile = serverboundHelloPacket.getGameProfile();
		if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
			this.state = ServerLoginPacketListenerImpl.State.KEY;
			this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.nonce));
		} else {
			this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
		}
	}

	@Override
	public void handleKey(ServerboundKeyPacket serverboundKeyPacket) {
		Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");
		PrivateKey privateKey = this.server.getKeyPair().getPrivate();

		final String string;
		try {
			if (!Arrays.equals(this.nonce, serverboundKeyPacket.getNonce(privateKey))) {
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
				GameProfile gameProfile = ServerLoginPacketListenerImpl.this.gameProfile;

				try {
					ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.server
						.getSessionService()
						.hasJoinedServer(new GameProfile(null, gameProfile.getName()), string, this.getAddress());
					if (ServerLoginPacketListenerImpl.this.gameProfile != null) {
						ServerLoginPacketListenerImpl.LOGGER
							.info("UUID of player {} is {}", ServerLoginPacketListenerImpl.this.gameProfile.getName(), ServerLoginPacketListenerImpl.this.gameProfile.getId());
						ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
					} else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
						ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
						ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.createFakeProfile(gameProfile);
						ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
					} else {
						ServerLoginPacketListenerImpl.this.disconnect(new TranslatableComponent("multiplayer.disconnect.unverified_username"));
						ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", gameProfile.getName());
					}
				} catch (AuthenticationUnavailableException var3) {
					if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
						ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
						ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.createFakeProfile(gameProfile);
						ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
					} else {
						ServerLoginPacketListenerImpl.this.disconnect(new TranslatableComponent("multiplayer.disconnect.authservers_down"));
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
	public void handleCustomQueryPacket(ServerboundCustomQueryPacket serverboundCustomQueryPacket) {
		this.disconnect(new TranslatableComponent("multiplayer.disconnect.unexpected_query_response"));
	}

	protected GameProfile createFakeProfile(GameProfile gameProfile) {
		UUID uUID = Player.createPlayerUUID(gameProfile.getName());
		return new GameProfile(uUID, gameProfile.getName());
	}

	static enum State {
		HELLO,
		KEY,
		AUTHENTICATING,
		NEGOTIATING,
		READY_TO_ACCEPT,
		DELAY_ACCEPT,
		ACCEPTED;
	}
}
