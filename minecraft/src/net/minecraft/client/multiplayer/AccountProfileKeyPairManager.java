package net.minecraft.client.multiplayer;

import com.google.common.base.Strings;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.MissingException;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse.KeyPair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class AccountProfileKeyPairManager implements ProfileKeyPairManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Duration MINIMUM_PROFILE_KEY_REFRESH_INTERVAL = Duration.ofHours(1L);
	private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys");
	private final UserApiService userApiService;
	private final Path profileKeyPairPath;
	private CompletableFuture<Optional<ProfileKeyPair>> keyPair = CompletableFuture.completedFuture(Optional.empty());
	private Instant nextProfileKeyRefreshTime = Instant.EPOCH;

	public AccountProfileKeyPairManager(UserApiService userApiService, UUID uUID, Path path) {
		this.userApiService = userApiService;
		this.profileKeyPairPath = path.resolve(PROFILE_KEY_PAIR_DIR).resolve(uUID + ".json");
	}

	@Override
	public CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair() {
		this.nextProfileKeyRefreshTime = Instant.now().plus(MINIMUM_PROFILE_KEY_REFRESH_INTERVAL);
		this.keyPair = this.keyPair.thenCompose(this::readOrFetchProfileKeyPair);
		return this.keyPair;
	}

	@Override
	public boolean shouldRefreshKeyPair() {
		return this.keyPair.isDone() && Instant.now().isAfter(this.nextProfileKeyRefreshTime)
			? (Boolean)((Optional)this.keyPair.join()).map(ProfileKeyPair::dueRefresh).orElse(true)
			: false;
	}

	private CompletableFuture<Optional<ProfileKeyPair>> readOrFetchProfileKeyPair(Optional<ProfileKeyPair> optional) {
		return CompletableFuture.supplyAsync(() -> {
			if (optional.isPresent() && !((ProfileKeyPair)optional.get()).dueRefresh()) {
				if (!SharedConstants.IS_RUNNING_IN_IDE) {
					this.writeProfileKeyPair(null);
				}

				return optional;
			} else {
				try {
					ProfileKeyPair profileKeyPair = this.fetchProfileKeyPair(this.userApiService);
					this.writeProfileKeyPair(profileKeyPair);
					return Optional.ofNullable(profileKeyPair);
				} catch (CryptException | MinecraftClientException | IOException var3) {
					LOGGER.error("Failed to retrieve profile key pair", (Throwable)var3);
					this.writeProfileKeyPair(null);
					return optional;
				}
			}
		}, Util.nonCriticalIoPool());
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
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				ProfileKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, profileKeyPair).ifSuccess(jsonElement -> {
					try {
						Files.createDirectories(this.profileKeyPairPath.getParent());
						Files.writeString(this.profileKeyPairPath, jsonElement.toString());
					} catch (Exception var3x) {
						LOGGER.error("Failed to write profile key pair file {}", this.profileKeyPairPath, var3x);
					}
				});
			}
		}
	}

	@Nullable
	private ProfileKeyPair fetchProfileKeyPair(UserApiService userApiService) throws CryptException, IOException {
		KeyPairResponse keyPairResponse = userApiService.getKeyPair();
		if (keyPairResponse != null) {
			ProfilePublicKey.Data data = parsePublicKey(keyPairResponse);
			return new ProfileKeyPair(
				Crypt.stringToPemRsaPrivateKey(keyPairResponse.keyPair().privateKey()), new ProfilePublicKey(data), Instant.parse(keyPairResponse.refreshedAfter())
			);
		} else {
			return null;
		}
	}

	private static ProfilePublicKey.Data parsePublicKey(KeyPairResponse keyPairResponse) throws CryptException {
		KeyPair keyPair = keyPairResponse.keyPair();
		if (keyPair != null
			&& !Strings.isNullOrEmpty(keyPair.publicKey())
			&& keyPairResponse.publicKeySignature() != null
			&& keyPairResponse.publicKeySignature().array().length != 0) {
			try {
				Instant instant = Instant.parse(keyPairResponse.expiresAt());
				PublicKey publicKey = Crypt.stringToRsaPublicKey(keyPair.publicKey());
				ByteBuffer byteBuffer = keyPairResponse.publicKeySignature();
				return new ProfilePublicKey.Data(instant, publicKey, byteBuffer.array());
			} catch (IllegalArgumentException | DateTimeException var5) {
				throw new CryptException(var5);
			}
		} else {
			throw new CryptException(new MissingException("Missing public key"));
		}
	}
}
