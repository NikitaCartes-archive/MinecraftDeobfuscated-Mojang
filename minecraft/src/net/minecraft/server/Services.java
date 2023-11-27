package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.SignatureValidator;

public record Services(
	MinecraftSessionService sessionService, ServicesKeySet servicesKeySet, GameProfileRepository profileRepository, GameProfileCache profileCache
) {
	private static final String USERID_CACHE_FILE = "usercache.json";

	public static Services create(YggdrasilAuthenticationService yggdrasilAuthenticationService, File file) {
		MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
		GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
		GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, "usercache.json"));
		return new Services(minecraftSessionService, yggdrasilAuthenticationService.getServicesKeySet(), gameProfileRepository, gameProfileCache);
	}

	@Nullable
	public SignatureValidator profileKeySignatureValidator() {
		return SignatureValidator.from(this.servicesKeySet, ServicesKeyType.PROFILE_KEY);
	}

	public boolean canValidateProfileKeys() {
		return !this.servicesKeySet.keys(ServicesKeyType.PROFILE_KEY).isEmpty();
	}
}
