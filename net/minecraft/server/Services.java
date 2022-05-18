/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.SignatureValidator;

public record Services(MinecraftSessionService sessionService, SignatureValidator serviceSignatureValidator, GameProfileRepository profileRepository, GameProfileCache profileCache) {
    private static final String USERID_CACHE_FILE = "usercache.json";

    public static Services create(YggdrasilAuthenticationService yggdrasilAuthenticationService, File file) {
        MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
        GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
        GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, USERID_CACHE_FILE));
        SignatureValidator signatureValidator = SignatureValidator.from(yggdrasilAuthenticationService.getServicesKey());
        return new Services(minecraftSessionService, signatureValidator, gameProfileRepository, gameProfileCache);
    }
}

