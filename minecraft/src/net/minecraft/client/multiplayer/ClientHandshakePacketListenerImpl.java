package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Crypt;
import net.minecraft.util.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	@Nullable
	private final Screen parent;
	private final Consumer<Component> updateStatus;
	private final Connection connection;
	private GameProfile localGameProfile;

	public ClientHandshakePacketListenerImpl(Connection connection, Minecraft minecraft, @Nullable Screen screen, Consumer<Component> consumer) {
		this.connection = connection;
		this.minecraft = minecraft;
		this.parent = screen;
		this.updateStatus = consumer;
	}

	@Override
	public void handleHello(ClientboundHelloPacket clientboundHelloPacket) {
		SecretKey secretKey = Crypt.generateSecretKey();
		PublicKey publicKey = clientboundHelloPacket.getPublicKey();
		String string = new BigInteger(Crypt.digestData(clientboundHelloPacket.getServerId(), publicKey, secretKey)).toString(16);
		ServerboundKeyPacket serverboundKeyPacket = new ServerboundKeyPacket(secretKey, publicKey, clientboundHelloPacket.getNonce());
		this.updateStatus.accept(new TranslatableComponent("connect.authorizing"));
		HttpUtil.DOWNLOAD_EXECUTOR.submit((Runnable)(() -> {
			Component component = this.authenticateServer(string);
			if (component != null) {
				if (this.minecraft.getCurrentServer() == null || !this.minecraft.getCurrentServer().isLan()) {
					this.connection.disconnect(component);
					return;
				}

				LOGGER.warn(component.getString());
			}

			this.updateStatus.accept(new TranslatableComponent("connect.encrypting"));
			this.connection.send(serverboundKeyPacket, future -> this.connection.setEncryptionKey(secretKey));
		}));
	}

	@Nullable
	private Component authenticateServer(String string) {
		try {
			this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getGameProfile(), this.minecraft.getUser().getAccessToken(), string);
			return null;
		} catch (AuthenticationUnavailableException var3) {
			return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.serversUnavailable"));
		} catch (InvalidCredentialsException var4) {
			return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.invalidSession"));
		} catch (AuthenticationException var5) {
			return new TranslatableComponent("disconnect.loginFailedInfo", var5.getMessage());
		}
	}

	private MinecraftSessionService getMinecraftSessionService() {
		return this.minecraft.getMinecraftSessionService();
	}

	@Override
	public void handleGameProfile(ClientboundGameProfilePacket clientboundGameProfilePacket) {
		this.updateStatus.accept(new TranslatableComponent("connect.joining"));
		this.localGameProfile = clientboundGameProfilePacket.getGameProfile();
		this.connection.setProtocol(ConnectionProtocol.PLAY);
		this.connection.setListener(new ClientPacketListener(this.minecraft, this.parent, this.connection, this.localGameProfile));
	}

	@Override
	public void onDisconnect(Component component) {
		if (this.parent != null && this.parent instanceof RealmsScreen) {
			this.minecraft.setScreen(new DisconnectedRealmsScreen(this.parent, CommonComponents.CONNECT_FAILED, component));
		} else {
			this.minecraft.setScreen(new DisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, component));
		}
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	@Override
	public void handleDisconnect(ClientboundLoginDisconnectPacket clientboundLoginDisconnectPacket) {
		this.connection.disconnect(clientboundLoginDisconnectPacket.getReason());
	}

	@Override
	public void handleCompression(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket) {
		if (!this.connection.isMemoryConnection()) {
			this.connection.setupCompression(clientboundLoginCompressionPacket.getCompressionThreshold());
		}
	}

	@Override
	public void handleCustomQuery(ClientboundCustomQueryPacket clientboundCustomQueryPacket) {
		this.updateStatus.accept(new TranslatableComponent("connect.negotiating"));
		this.connection.send(new ServerboundCustomQueryPacket(clientboundCustomQueryPacket.getTransactionId(), null));
	}
}
