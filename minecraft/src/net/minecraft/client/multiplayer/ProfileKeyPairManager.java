package net.minecraft.client.multiplayer;

import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.User;
import net.minecraft.world.entity.player.ProfileKeyPair;

@Environment(EnvType.CLIENT)
public interface ProfileKeyPairManager {
	ProfileKeyPairManager EMPTY_KEY_MANAGER = new ProfileKeyPairManager() {
		@Override
		public CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair() {
			return CompletableFuture.completedFuture(Optional.empty());
		}

		@Override
		public boolean shouldRefreshKeyPair() {
			return false;
		}
	};

	static ProfileKeyPairManager create(UserApiService userApiService, User user, Path path) {
		return (ProfileKeyPairManager)(user.getType() == User.Type.MSA
			? new AccountProfileKeyPairManager(userApiService, user.getProfileId(), path)
			: EMPTY_KEY_MANAGER);
	}

	CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair();

	boolean shouldRefreshKeyPair();
}
