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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ProfileKeyPairManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys", new String[0]);
    private final UserApiService userApiService;
    private final Path profileKeyPairPath;
    private CompletableFuture<Optional<Result>> keyPair;

    public ProfileKeyPairManager(UserApiService userApiService, UUID uUID, Path path) {
        this.userApiService = userApiService;
        this.profileKeyPairPath = path.resolve(PROFILE_KEY_PAIR_DIR).resolve(uUID + ".json");
        this.keyPair = CompletableFuture.supplyAsync(() -> this.readProfileKeyPair().filter(profileKeyPair -> !profileKeyPair.publicKey().data().hasExpired()), Util.backgroundExecutor()).thenCompose(this::readOrFetchProfileKeyPair);
    }

    public CompletableFuture<Optional<ProfilePublicKey.Data>> preparePublicKey() {
        this.keyPair = this.keyPair.thenCompose(optional -> {
            Optional<ProfileKeyPair> optional2 = optional.map(Result::keyPair);
            return this.readOrFetchProfileKeyPair(optional2);
        });
        return this.keyPair.thenApply(optional -> optional.map(result -> result.keyPair().publicKey().data()));
    }

    private CompletableFuture<Optional<Result>> readOrFetchProfileKeyPair(Optional<ProfileKeyPair> optional2) {
        return CompletableFuture.supplyAsync(() -> {
            if (optional2.isPresent() && !((ProfileKeyPair)optional2.get()).dueRefresh()) {
                if (!SharedConstants.IS_RUNNING_IN_IDE) {
                    this.writeProfileKeyPair(null);
                } else {
                    return optional2;
                }
            }
            try {
                ProfileKeyPair profileKeyPair = this.fetchProfileKeyPair(this.userApiService);
                this.writeProfileKeyPair(profileKeyPair);
                return Optional.of(profileKeyPair);
            } catch (MinecraftClientException | IOException | CryptException exception) {
                LOGGER.error("Failed to retrieve profile key pair", exception);
                this.writeProfileKeyPair(null);
                return optional2;
            }
        }, Util.backgroundExecutor()).thenApply(optional -> optional.map(Result::new));
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
            ProfilePublicKey.Data data = ProfileKeyPairManager.parsePublicKey(keyPairResponse);
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

    @Nullable
    public Signer signer() {
        return this.keyPair.join().map(Result::signer).orElse(null);
    }

    public Optional<ProfilePublicKey> profilePublicKey() {
        return this.keyPair.join().map(result -> result.keyPair().publicKey());
    }

    @Environment(value=EnvType.CLIENT)
    record Result(ProfileKeyPair keyPair, Signer signer) {
        public Result(ProfileKeyPair profileKeyPair) {
            this(profileKeyPair, Signer.from(profileKeyPair.privateKey(), "SHA256withRSA"));
        }
    }
}

