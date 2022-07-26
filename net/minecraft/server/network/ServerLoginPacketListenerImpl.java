/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ThrowingComponent;
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
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl
implements TickablePacketListener,
ServerLoginPacketListener {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_LOGIN = 600;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Component MISSING_PROFILE_PUBLIC_KEY = Component.translatable("multiplayer.disconnect.missing_public_key");
    private static final Component INVALID_SIGNATURE = Component.translatable("multiplayer.disconnect.invalid_public_key_signature");
    private static final Component INVALID_PUBLIC_KEY = Component.translatable("multiplayer.disconnect.invalid_public_key");
    private final byte[] nonce;
    final MinecraftServer server;
    public final Connection connection;
    State state = State.HELLO;
    private int tick;
    @Nullable
    GameProfile gameProfile;
    private final String serverId = "";
    @Nullable
    private ServerPlayer delayedAcceptPlayer;
    @Nullable
    private ProfilePublicKey.Data profilePublicKeyData;

    public ServerLoginPacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
        this.server = minecraftServer;
        this.connection = connection;
        this.nonce = Ints.toByteArray(RANDOM.nextInt());
    }

    @Override
    public void tick() {
        ServerPlayer serverPlayer;
        if (this.state == State.READY_TO_ACCEPT) {
            this.handleAcceptedLogin();
        } else if (this.state == State.DELAY_ACCEPT && (serverPlayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId())) == null) {
            this.state = State.READY_TO_ACCEPT;
            this.placeNewPlayer(this.delayedAcceptPlayer);
            this.delayedAcceptPlayer = null;
        }
        if (this.tick++ == 600) {
            this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
        }
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    public void disconnect(Component component) {
        try {
            LOGGER.info("Disconnecting {}: {}", (Object)this.getUserName(), (Object)component.getString());
            this.connection.send(new ClientboundLoginDisconnectPacket(component));
            this.connection.disconnect(component);
        } catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    public void handleAcceptedLogin() {
        ProfilePublicKey profilePublicKey;
        block11: {
            profilePublicKey = null;
            if (!this.gameProfile.isComplete()) {
                this.gameProfile = this.createFakeProfile(this.gameProfile);
            } else {
                try {
                    SignatureValidator signatureValidator = this.server.getServiceSignatureValidator();
                    profilePublicKey = ServerLoginPacketListenerImpl.validatePublicKey(this.profilePublicKeyData, this.gameProfile.getId(), signatureValidator, this.server.enforceSecureProfile());
                } catch (PublicKeyValidationException publicKeyValidationException) {
                    LOGGER.error(publicKeyValidationException.getMessage(), publicKeyValidationException.getCause());
                    if (this.connection.isMemoryConnection()) break block11;
                    this.disconnect(publicKeyValidationException.getComponent());
                    return;
                }
            }
        }
        Component component = this.server.getPlayerList().canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
        if (component != null) {
            this.disconnect(component);
        } else {
            this.state = State.ACCEPTED;
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true)));
            }
            this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
            ServerPlayer serverPlayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
            try {
                ServerPlayer serverPlayer2 = this.server.getPlayerList().getPlayerForLogin(this.gameProfile, profilePublicKey);
                if (serverPlayer != null) {
                    this.state = State.DELAY_ACCEPT;
                    this.delayedAcceptPlayer = serverPlayer2;
                } else {
                    this.placeNewPlayer(serverPlayer2);
                }
            } catch (Exception exception) {
                LOGGER.error("Couldn't place player in world", exception);
                MutableComponent component2 = Component.translatable("multiplayer.disconnect.invalid_player_data");
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
        LOGGER.info("{} lost connection: {}", (Object)this.getUserName(), (Object)component.getString());
    }

    public String getUserName() {
        if (this.gameProfile != null) {
            return this.gameProfile + " (" + this.connection.getRemoteAddress() + ")";
        }
        return String.valueOf(this.connection.getRemoteAddress());
    }

    @Nullable
    private static ProfilePublicKey validatePublicKey(@Nullable ProfilePublicKey.Data data, UUID uUID, SignatureValidator signatureValidator, boolean bl) throws PublicKeyValidationException {
        try {
            if (data == null) {
                if (bl) {
                    throw new PublicKeyValidationException(MISSING_PROFILE_PUBLIC_KEY);
                }
                return null;
            }
            return ProfilePublicKey.createValidated(signatureValidator, uUID, data);
        } catch (InsecurePublicKeyException.MissingException missingException) {
            if (bl) {
                throw new PublicKeyValidationException(INVALID_SIGNATURE, (Throwable)missingException);
            }
            return null;
        } catch (CryptException cryptException) {
            throw new PublicKeyValidationException(INVALID_PUBLIC_KEY, (Throwable)cryptException);
        } catch (Exception exception) {
            throw new PublicKeyValidationException(INVALID_SIGNATURE, (Throwable)exception);
        }
    }

    @Override
    public void handleHello(ServerboundHelloPacket serverboundHelloPacket) {
        Validate.validState(this.state == State.HELLO, "Unexpected hello packet", new Object[0]);
        Validate.validState(ServerLoginPacketListenerImpl.isValidUsername(serverboundHelloPacket.name()), "Invalid characters in username", new Object[0]);
        this.profilePublicKeyData = serverboundHelloPacket.publicKey().orElse(null);
        GameProfile gameProfile = this.server.getSingleplayerProfile();
        if (gameProfile != null && serverboundHelloPacket.name().equalsIgnoreCase(gameProfile.getName())) {
            this.gameProfile = gameProfile;
            this.state = State.READY_TO_ACCEPT;
            return;
        }
        this.gameProfile = new GameProfile(null, serverboundHelloPacket.name());
        if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            this.state = State.KEY;
            this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.nonce));
        } else {
            this.state = State.READY_TO_ACCEPT;
        }
    }

    public static boolean isValidUsername(String string) {
        return string.chars().filter(i -> i <= 32 || i >= 127).findAny().isEmpty();
    }

    @Override
    public void handleKey(ServerboundKeyPacket serverboundKeyPacket) {
        String string;
        Validate.validState(this.state == State.KEY, "Unexpected key packet", new Object[0]);
        try {
            ProfilePublicKey profilePublicKey;
            PrivateKey privateKey = this.server.getKeyPair().getPrivate();
            if (this.profilePublicKeyData != null ? !serverboundKeyPacket.isChallengeSignatureValid(this.nonce, profilePublicKey = ProfilePublicKey.createTrusted(this.profilePublicKeyData)) : !serverboundKeyPacket.isNonceValid(this.nonce, privateKey)) {
                throw new IllegalStateException("Protocol error");
            }
            SecretKey secretKey = serverboundKeyPacket.getSecretKey(privateKey);
            Cipher cipher = Crypt.getCipher(2, secretKey);
            Cipher cipher2 = Crypt.getCipher(1, secretKey);
            string = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretKey)).toString(16);
            this.state = State.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher2);
        } catch (CryptException cryptException) {
            throw new IllegalStateException("Protocol error", cryptException);
        }
        Thread thread = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()){

            @Override
            public void run() {
                GameProfile gameProfile = ServerLoginPacketListenerImpl.this.gameProfile;
                try {
                    ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.server.getSessionService().hasJoinedServer(new GameProfile(null, gameProfile.getName()), string, this.getAddress());
                    if (ServerLoginPacketListenerImpl.this.gameProfile != null) {
                        LOGGER.info("UUID of player {} is {}", (Object)ServerLoginPacketListenerImpl.this.gameProfile.getName(), (Object)ServerLoginPacketListenerImpl.this.gameProfile.getId());
                        ServerLoginPacketListenerImpl.this.state = State.READY_TO_ACCEPT;
                    } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.gameProfile = gameProfile;
                        ServerLoginPacketListenerImpl.this.state = State.READY_TO_ACCEPT;
                    } else {
                        ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        LOGGER.error("Username '{}' tried to join with an invalid session", (Object)gameProfile.getName());
                    }
                } catch (AuthenticationUnavailableException authenticationUnavailableException) {
                    if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.gameProfile = gameProfile;
                        ServerLoginPacketListenerImpl.this.state = State.READY_TO_ACCEPT;
                    }
                    ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                    LOGGER.error("Couldn't verify username because servers are unavailable");
                }
            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketAddress = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
                return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && socketAddress instanceof InetSocketAddress ? ((InetSocketAddress)socketAddress).getAddress() : null;
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    public void handleCustomQueryPacket(ServerboundCustomQueryPacket serverboundCustomQueryPacket) {
        this.disconnect(Component.translatable("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile createFakeProfile(GameProfile gameProfile) {
        UUID uUID = UUIDUtil.createOfflinePlayerUUID(gameProfile.getName());
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

    static class PublicKeyValidationException
    extends ThrowingComponent {
        public PublicKeyValidationException(Component component) {
            super(component);
        }

        public PublicKeyValidationException(Component component, Throwable throwable) {
            super(component, throwable);
        }
    }
}

