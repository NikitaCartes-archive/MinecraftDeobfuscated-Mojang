package net.minecraft.client.multiplayer;

import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ProfileKeyPairManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys");
	private final Path profileKeyPairPath;
	private final CompletableFuture<ProfileKeyPair> profileKeyPair;

	public ProfileKeyPairManager(UserApiService userApiService, UUID uUID, Path path) {
		this.profileKeyPairPath = path.resolve(PROFILE_KEY_PAIR_DIR).resolve(uUID + ".json");
		this.profileKeyPair = this.readOrFetchProfileKeyPair(userApiService);
	}

	private CompletableFuture<ProfileKeyPair> readOrFetchProfileKeyPair(UserApiService userApiService) {
		return CompletableFuture.supplyAsync(() -> {
			Optional<ProfileKeyPair> optional = this.readProfileKeyPair().filter(profileKeyPair -> !profileKeyPair.publicKey().data().hasExpired());
			if (optional.isPresent() && !((ProfileKeyPair)optional.get()).dueRefresh()) {
				return (ProfileKeyPair)optional.get();
			} else {
				try {
					ProfileKeyPair profileKeyPair = this.fetchProfileKeyPair(userApiService);
					this.writeProfileKeyPair(profileKeyPair);
					return profileKeyPair;
				} catch (CryptException | MinecraftClientException | IOException var4) {
					LOGGER.error("Failed to retrieve profile key pair", (Throwable)var4);
					this.writeProfileKeyPair(null);
					return (ProfileKeyPair)optional.orElse(null);
				}
			}
		}, Util.backgroundExecutor());
	}

	private Optional<ProfileKeyPair> readProfileKeyPair() {
		if (Files.notExists(this.profileKeyPairPath, new LinkOption[0])) {
			return Optional.empty();
		} else {
			try {
				BufferedReader bufferedReader = Files.newBufferedReader(this.profileKeyPairPath);

				Optional var2;
				try {
					var2 = ProfileKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader)).result();
				} catch (Throwable var5) {
					if (bufferedReader != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var4) {
							var5.addSuppressed(var4);
						}
					}

					throw var5;
				}

				if (bufferedReader != null) {
					bufferedReader.close();
				}

				return var2;
			} catch (Exception var6) {
				LOGGER.error("Failed to read profile key pair file {}", this.profileKeyPairPath, var6);
				return Optional.empty();
			}
		}
	}

	private void writeProfileKeyPair(@Nullable ProfileKeyPair profileKeyPair) {
		try {
			Files.deleteIfExists(this.profileKeyPairPath);
		} catch (IOException var3) {
			LOGGER.error("Failed to delete profile key pair file {}", this.profileKeyPairPath, var3);
		}

		if (profileKeyPair != null) {
			ProfileKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, profileKeyPair).result().ifPresent(jsonElement -> {
				try {
					Files.createDirectories(this.profileKeyPairPath.getParent());
					Files.writeString(this.profileKeyPairPath, jsonElement.toString());
				} catch (Exception var3x) {
					LOGGER.error("Failed to write profile key pair file {}", this.profileKeyPairPath, var3x);
				}
			});
		}
	}

	private ProfileKeyPair fetchProfileKeyPair(UserApiService userApiService) throws CryptException, IOException {
		KeyPairResponse keyPairResponse = userApiService.getKeyPair();
		if (keyPairResponse != null) {
			ProfilePublicKey.Data data = new ProfilePublicKey.Data(
				Instant.parse(keyPairResponse.getExpiresAt()), keyPairResponse.getPublicKey(), keyPairResponse.getPublicKeySignature()
			);
			return new ProfileKeyPair(
				Crypt.stringToPemRsaPrivateKey(keyPairResponse.getPrivateKey()), ProfilePublicKey.parseTrusted(data), Instant.parse(keyPairResponse.getRefreshedAfter())
			);
		} else {
			throw new IOException("Could not retrieve profile key pair");
		}
	}

	@Nullable
	public Signature createSignature() throws GeneralSecurityException {
		PrivateKey privateKey = this.profilePrivateKey();
		if (privateKey == null) {
			return null;
		} else {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			return signature;
		}
	}

	public Optional<ProfilePublicKey.Data> profilePublicKeyData() {
		ProfilePublicKey profilePublicKey = this.profilePublicKey();
		return Optional.ofNullable(profilePublicKey).map(ProfilePublicKey::data);
	}

	@Nullable
	public ProfilePublicKey profilePublicKey() {
		ProfileKeyPair profileKeyPair = (ProfileKeyPair)this.profileKeyPair.join();
		return profileKeyPair != null ? profileKeyPair.publicKey() : null;
	}

	@Nullable
	private PrivateKey profilePrivateKey() {
		ProfileKeyPair profileKeyPair = (ProfileKeyPair)this.profileKeyPair.join();
		return profileKeyPair != null ? profileKeyPair.privateKey() : null;
	}
}
