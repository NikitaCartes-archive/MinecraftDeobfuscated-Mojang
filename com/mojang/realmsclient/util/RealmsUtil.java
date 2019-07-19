/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;

@Environment(value=EnvType.CLIENT)
public class RealmsUtil {
    private static final YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(Realms.getProxy(), UUID.randomUUID().toString());
    private static final MinecraftSessionService sessionService = authenticationService.createMinecraftSessionService();
    public static LoadingCache<String, GameProfile> gameProfileCache = CacheBuilder.newBuilder().expireAfterWrite(60L, TimeUnit.MINUTES).build(new CacheLoader<String, GameProfile>(){

        @Override
        public GameProfile load(String string) throws Exception {
            GameProfile gameProfile = sessionService.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(string), null), false);
            if (gameProfile == null) {
                throw new Exception("Couldn't get profile");
            }
            return gameProfile;
        }

        @Override
        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((String)object);
        }
    });

    public static String uuidToName(String string) throws Exception {
        GameProfile gameProfile = gameProfileCache.get(string);
        return gameProfile.getName();
    }

    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(String string) {
        try {
            GameProfile gameProfile = gameProfileCache.get(string);
            return sessionService.getTextures(gameProfile, false);
        } catch (Exception exception) {
            return new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
        }
    }

    public static void browseTo(String string) {
        Realms.openUri(string);
    }

    public static String convertToAgePresentation(Long long_) {
        if (long_ < 0L) {
            return "right now";
        }
        long l = long_ / 1000L;
        if (l < 60L) {
            return (l == 1L ? "1 second" : l + " seconds") + " ago";
        }
        if (l < 3600L) {
            long m = l / 60L;
            return (m == 1L ? "1 minute" : m + " minutes") + " ago";
        }
        if (l < 86400L) {
            long m = l / 3600L;
            return (m == 1L ? "1 hour" : m + " hours") + " ago";
        }
        long m = l / 86400L;
        return (m == 1L ? "1 day" : m + " days") + " ago";
    }
}

