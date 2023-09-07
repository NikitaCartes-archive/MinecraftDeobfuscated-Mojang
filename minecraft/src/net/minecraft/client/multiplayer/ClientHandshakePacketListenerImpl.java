package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.ForcedUsernameChangeException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.util.Crypt;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Minecraft minecraft;
	@Nullable
	private final ServerData serverData;
	@Nullable
	private final Screen parent;
	private final Consumer<Component> updateStatus;
	private final Connection connection;
	private final boolean newWorld;
	@Nullable
	private final Duration worldLoadDuration;
	@Nullable
	private String minigameName;
	private final AtomicReference<ClientHandshakePacketListenerImpl.State> state = new AtomicReference(ClientHandshakePacketListenerImpl.State.CONNECTING);

	public ClientHandshakePacketListenerImpl(
		Connection connection,
		Minecraft minecraft,
		@Nullable ServerData serverData,
		@Nullable Screen screen,
		boolean bl,
		@Nullable Duration duration,
		Consumer<Component> consumer
	) {
		this.connection = connection;
		this.minecraft = minecraft;
		this.serverData = serverData;
		this.parent = screen;
		this.updateStatus = consumer;
		this.newWorld = bl;
		this.worldLoadDuration = duration;
	}

	private void switchState(ClientHandshakePacketListenerImpl.State state) {
		ClientHandshakePacketListenerImpl.State state2 = (ClientHandshakePacketListenerImpl.State)this.state.updateAndGet(state2x -> {
			if (!state.fromStates.contains(state2x)) {
				throw new IllegalStateException("Tried to switch to " + state + " from " + state2x + ", but expected one of " + state.fromStates);
			} else {
				return state;
			}
		});
		this.updateStatus.accept(state2.message);
	}

	@Override
	public void handleHello(ClientboundHelloPacket clientboundHelloPacket) {
		this.switchState(ClientHandshakePacketListenerImpl.State.AUTHORIZING);

		Cipher cipher;
		Cipher cipher2;
		String string;
		ServerboundKeyPacket serverboundKeyPacket;
		try {
			SecretKey secretKey = Crypt.generateSecretKey();
			PublicKey publicKey = clientboundHelloPacket.getPublicKey();
			string = new BigInteger(Crypt.digestData(clientboundHelloPacket.getServerId(), publicKey, secretKey)).toString(16);
			cipher = Crypt.getCipher(2, secretKey);
			cipher2 = Crypt.getCipher(1, secretKey);
			byte[] bs = clientboundHelloPacket.getChallenge();
			serverboundKeyPacket = new ServerboundKeyPacket(secretKey, publicKey, bs);
		} catch (Exception var9) {
			throw new IllegalStateException("Protocol error", var9);
		}

		HttpUtil.DOWNLOAD_EXECUTOR.submit((Runnable)(() -> {
			Component component = this.authenticateServer(string);
			if (component != null) {
				if (this.serverData == null || !this.serverData.isLan()) {
					this.connection.disconnect(component);
					return;
				}

				LOGGER.warn(component.getString());
			}

			this.switchState(ClientHandshakePacketListenerImpl.State.ENCRYPTING);
			this.connection.send(serverboundKeyPacket, PacketSendListener.thenRun(() -> this.connection.setEncryptionKey(cipher, cipher2)));
		}));
	}

	@Nullable
	private Component authenticateServer(String string) {
		try {
			this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getProfileId(), this.minecraft.getUser().getAccessToken(), string);
			return null;
		} catch (AuthenticationUnavailableException var3) {
			return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.serversUnavailable"));
		} catch (InvalidCredentialsException var4) {
			return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.invalidSession"));
		} catch (InsufficientPrivilegesException var5) {
			return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
		} catch (ForcedUsernameChangeException | UserBannedException var6) {
			return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.userBanned"));
		} catch (AuthenticationException var7) {
			return Component.translatable("disconnect.loginFailedInfo", var7.getMessage());
		}
	}

	private MinecraftSessionService getMinecraftSessionService() {
		return this.minecraft.getMinecraftSessionService();
	}

	@Override
	public void handleGameProfile(ClientboundGameProfilePacket clientboundGameProfilePacket) {
		this.switchState(ClientHandshakePacketListenerImpl.State.JOINING);
		GameProfile gameProfile = clientboundGameProfilePacket.getGameProfile();
		this.connection.send(new ServerboundLoginAcknowledgedPacket());
		this.connection
			.setListener(
				new ClientConfigurationPacketListenerImpl(
					this.minecraft,
					this.connection,
					new CommonListenerCookie(
						gameProfile,
						this.minecraft.getTelemetryManager().createWorldSessionManager(this.newWorld, this.worldLoadDuration, this.minigameName),
						ClientRegistryLayer.createRegistryAccess().compositeAccess(),
						FeatureFlags.DEFAULT_FLAGS,
						null,
						this.serverData,
						this.parent
					)
				)
			);
		this.connection.send(new ServerboundCustomPayloadPacket(new BrandPayload(ClientBrandRetriever.getClientModName())));
		this.connection.send(new ServerboundClientInformationPacket(this.minecraft.options.buildPlayerInformation()));
	}

	@Override
	public void onDisconnect(Component component) {
		if (this.serverData != null && this.serverData.isRealm()) {
			this.minecraft.setScreen(new DisconnectedRealmsScreen(this.parent, CommonComponents.CONNECT_FAILED, component));
		} else {
			this.minecraft.setScreen(new DisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, component));
		}
	}

	@Override
	public boolean isAcceptingMessages() {
		return this.connection.isConnected();
	}

	@Override
	public void handleDisconnect(ClientboundLoginDisconnectPacket clientboundLoginDisconnectPacket) {
		this.connection.disconnect(clientboundLoginDisconnectPacket.getReason());
	}

	@Override
	public void handleCompression(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket) {
		if (!this.connection.isMemoryConnection()) {
			this.connection.setupCompression(clientboundLoginCompressionPacket.getCompressionThreshold(), false);
		}
	}

	@Override
	public void handleCustomQuery(ClientboundCustomQueryPacket clientboundCustomQueryPacket) {
		this.updateStatus.accept(Component.translatable("connect.negotiating"));
		this.connection.send(new ServerboundCustomQueryAnswerPacket(clientboundCustomQueryPacket.transactionId(), null));
	}

	public void setMinigameName(String string) {
		this.minigameName = string;
	}

	@Environment(EnvType.CLIENT)
	static enum State {
		CONNECTING(Component.translatable("connect.connecting"), Set.of()),
		AUTHORIZING(Component.translatable("connect.authorizing"), Set.of(CONNECTING)),
		ENCRYPTING(Component.translatable("connect.encrypting"), Set.of(AUTHORIZING)),
		JOINING(Component.translatable("connect.joining"), Set.of(ENCRYPTING, CONNECTING));

		final Component message;
		final Set<ClientHandshakePacketListenerImpl.State> fromStates;

		private State(Component component, Set<ClientHandshakePacketListenerImpl.State> set) {
			this.message = component;
			this.fromStates = set;
		}
	}
}
