/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ProfileKeyPairManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys", new String[0]);
    private final Path profileKeyPairPath;
    private final CompletableFuture<ProfileKeyPair> profileKeyPair;

    public ProfileKeyPairManager(UserApiService userApiService, UUID uUID, Path path) {
        this.profileKeyPairPath = path.resolve(PROFILE_KEY_PAIR_DIR).resolve(uUID + ".json");
        this.profileKeyPair = this.readOrFetchProfileKeyPair(userApiService);
    }

    private CompletableFuture<ProfileKeyPair> readOrFetchProfileKeyPair(UserApiService userApiService) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ProfileKeyPair> optional = this.readProfileKeyPair().filter(profileKeyPair -> !profileKeyPair.publicKey().hasExpired());
            if (optional.isPresent() && !optional.get().dueRefresh()) {
                return optional.get();
            }
            try {
                ProfileKeyPair profileKeyPair2 = this.fetchProfileKeyPair(userApiService);
                this.writeProfileKeyPair(profileKeyPair2);
                return profileKeyPair2;
            } catch (IOException | CryptException exception) {
                LOGGER.error("Failed to retrieve profile key pair", exception);
                this.writeProfileKeyPair(null);
                return optional.orElse(null);
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
            return new ProfileKeyPair(Crypt.stringToPemRsaPrivateKey(keyPairResponse.getPrivateKey()), new ProfilePublicKey(Instant.parse(keyPairResponse.getExpiresAt()), keyPairResponse.getPublicKey(), keyPairResponse.getPublicKeySignature()), Instant.parse(keyPairResponse.getRefreshedAfter()));
        }
        throw new IOException("Could not retrieve profile key pair");
    }

    @Nullable
    public Signature createSignature() throws GeneralSecurityException {
        PrivateKey privateKey = this.profilePrivateKey();
        if (privateKey == null) {
            return null;
        }
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        return signature;
    }

    @Nullable
    public ProfilePublicKey profilePublicKey() {
        ProfileKeyPair profileKeyPair = this.profileKeyPair.join();
        return profileKeyPair != null ? profileKeyPair.publicKey() : null;
    }

    @Nullable
    private PrivateKey profilePrivateKey() {
        ProfileKeyPair profileKeyPair = this.profileKeyPair.join();
        return profileKeyPair != null ? profileKeyPair.privateKey() : null;
    }
}

