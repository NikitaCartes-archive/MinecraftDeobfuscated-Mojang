/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import com.google.common.base.Strings;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class AccountProfileKeyPairManager
implements ProfileKeyPairManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Duration MINIMUM_PROFILE_KEY_REFRESH_INTERVAL = Duration.ofHours(1L);
    private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys", new String[0]);
    private final UserApiService userApiService;
    private final Path profileKeyPairPath;
    private CompletableFuture<Optional<ProfileKeyPair>> keyPair;
    private Instant nextProfileKeyRefreshTime = Instant.EPOCH;

    public AccountProfileKeyPairManager(UserApiService userApiService, UUID uUID, Path path) {
        this.userApiService = userApiService;
        this.profileKeyPairPath = path.resolve(PROFILE_KEY_PAIR_DIR).resolve(uUID + ".json");
        this.keyPair = CompletableFuture.supplyAsync(() -> this.readProfileKeyPair().filter(profileKeyPair -> !profileKeyPair.publicKey().data().hasExpired()), Util.backgroundExecutor()).thenCompose(this::readOrFetchProfileKeyPair);
    }

    @Override
    public CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair() {
        this.nextProfileKeyRefreshTime = Instant.now().plus(MINIMUM_PROFILE_KEY_REFRESH_INTERVAL);
        this.keyPair = this.keyPair.thenCompose(this::readOrFetchProfileKeyPair);
        return this.keyPair;
    }

    @Override
    public boolean shouldRefreshKeyPair() {
        if (this.keyPair.isDone() && Instant.now().isAfter(this.nextProfileKeyRefreshTime)) {
            return this.keyPair.join().map(ProfileKeyPair::dueRefresh).orElse(true);
        }
        return false;
    }

    private CompletableFuture<Optional<ProfileKeyPair>> readOrFetchProfileKeyPair(Optional<ProfileKeyPair> optional) {
        return CompletableFuture.supplyAsync(() -> {
            if (optional.isPresent() && !((ProfileKeyPair)optional.get()).dueRefresh()) {
                if (!SharedConstants.IS_RUNNING_IN_IDE) {
                    this.writeProfileKeyPair(null);
                }
                return optional;
            }
            try {
                ProfileKeyPair profileKeyPair = this.fetchProfileKeyPair(this.userApiService);
                this.writeProfileKeyPair(profileKeyPair);
                return Optional.of(profileKeyPair);
            } catch (MinecraftClientException | IOException | CryptException exception) {
                LOGGER.error("Failed to retrieve profile key pair", exception);
                this.writeProfileKeyPair(null);
                return optional;
            }
        }, Util.backgroundExecutor());
    }

    private Optional<ProfileKeyPair> readProfileKeyPair() {
        Optional<ProfileKeyPair> optional;
        block9: {
            if (Files.notExists(this.profileKeyPairPath, new LinkOption[0])) {
                return Optional.empty();
            }
            BufferedReader bufferedReader = Files.newBufferedReader(this.profileKeyPairPath);
            try {
                optional = ProfileKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader)).result();
                if (bufferedReader == null) break block9;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (Exception exception) {
                    LOGGER.error("Failed to read profile key pair file {}", (Object)this.profileKeyPairPath, (Object)exception);
                    return Optional.empty();
                }
            }
            bufferedReader.close();
        }
        return optional;
    }

    private void writeProfileKeyPair(@Nullable ProfileKeyPair profileKeyPair) {
        try {
            Files.deleteIfExists(this.profileKeyPairPath);
        } catch (IOException iOException) {
            LOGGER.error("Failed to delete profile key pair file {}", (Object)this.profileKeyPairPath, (Object)iOException);
        }
        if (profileKeyPair == null) {
            return;
        }
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            return;
        }
        ProfileKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, profileKeyPair).result().ifPresent(jsonElement -> {
            try {
                Files.createDirectories(this.profileKeyPairPath.getParent(), new FileAttribute[0]);
                Files.writeString(this.profileKeyPairPath, (CharSequence)jsonElement.toString(), new OpenOption[0]);
            } catch (Exception exception) {
                LOGGER.error("Failed to write profile key pair file {}", (Object)this.profileKeyPairPath, (Object)exception);
            }
        });
    }

    private ProfileKeyPair fetchProfileKeyPair(UserApiService userApiService) throws CryptException, IOException {
        KeyPairResponse keyPairResponse = userApiService.getKeyPair();
        if (keyPairResponse != null) {
            ProfilePublicKey.Data data = AccountProfileKeyPairManager.parsePublicKey(keyPairResponse);
            return new ProfileKeyPair(Crypt.stringToPemRsaPrivateKey(keyPairResponse.getPrivateKey()), new ProfilePublicKey(data), Instant.parse(keyPairResponse.getRefreshedAfter()));
        }
        throw new IOException("Could not retrieve profile key pair");
    }

    private static ProfilePublicKey.Data parsePublicKey(KeyPairResponse keyPairResponse) throws CryptException {
        if (Strings.isNullOrEmpty(keyPairResponse.getPublicKey()) || keyPairResponse.getPublicKeySignature() == null || keyPairResponse.getPublicKeySignature().array().length == 0) {
            throw new CryptException(new InsecurePublicKeyException.MissingException());
        }
        try {
            Instant instant = Instant.parse(keyPairResponse.getExpiresAt());
            PublicKey publicKey = Crypt.stringToRsaPublicKey(keyPairResponse.getPublicKey());
            ByteBuffer byteBuffer = keyPairResponse.getPublicKeySignature();
            return new ProfilePublicKey.Data(instant, publicKey, byteBuffer.array());
        } catch (IllegalArgumentException | DateTimeException runtimeException) {
            throw new CryptException(runtimeException);
        }
    }
}

