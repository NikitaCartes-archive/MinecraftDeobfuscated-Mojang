/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.HttpTextureProcessor;
import net.minecraft.client.renderer.MobSkinTextureProcessor;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkinManager {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    private final TextureManager textureManager;
    private final File skinsDirectory;
    private final MinecraftSessionService sessionService;
    private final LoadingCache<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> insecureSkinCache;

    public SkinManager(TextureManager textureManager, File file, MinecraftSessionService minecraftSessionService) {
        this.textureManager = textureManager;
        this.skinsDirectory = file;
        this.sessionService = minecraftSessionService;
        this.insecureSkinCache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>(){

            @Override
            public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(GameProfile gameProfile) throws Exception {
                try {
                    return Minecraft.getInstance().getMinecraftSessionService().getTextures(gameProfile, false);
                } catch (Throwable throwable) {
                    return Maps.newHashMap();
                }
            }

            @Override
            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((GameProfile)object);
            }
        });
    }

    public ResourceLocation registerTexture(MinecraftProfileTexture minecraftProfileTexture, MinecraftProfileTexture.Type type) {
        return this.registerTexture(minecraftProfileTexture, type, null);
    }

    public ResourceLocation registerTexture(final MinecraftProfileTexture minecraftProfileTexture, final MinecraftProfileTexture.Type type, final @Nullable SkinTextureCallback skinTextureCallback) {
        String string = Hashing.sha1().hashUnencodedChars(minecraftProfileTexture.getHash()).toString();
        final ResourceLocation resourceLocation = new ResourceLocation("skins/" + string);
        TextureObject textureObject = this.textureManager.getTexture(resourceLocation);
        if (textureObject != null) {
            if (skinTextureCallback != null) {
                skinTextureCallback.onSkinTextureAvailable(type, resourceLocation, minecraftProfileTexture);
            }
        } else {
            File file = new File(this.skinsDirectory, string.length() > 2 ? string.substring(0, 2) : "xx");
            File file2 = new File(file, string);
            final MobSkinTextureProcessor httpTextureProcessor = type == MinecraftProfileTexture.Type.SKIN ? new MobSkinTextureProcessor() : null;
            HttpTexture httpTexture = new HttpTexture(file2, minecraftProfileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), new HttpTextureProcessor(){

                @Override
                public NativeImage process(NativeImage nativeImage) {
                    if (httpTextureProcessor != null) {
                        return httpTextureProcessor.process(nativeImage);
                    }
                    return nativeImage;
                }

                @Override
                public void onTextureDownloaded() {
                    if (httpTextureProcessor != null) {
                        httpTextureProcessor.onTextureDownloaded();
                    }
                    if (skinTextureCallback != null) {
                        skinTextureCallback.onSkinTextureAvailable(type, resourceLocation, minecraftProfileTexture);
                    }
                }
            });
            this.textureManager.register(resourceLocation, httpTexture);
        }
        return resourceLocation;
    }

    public void registerSkins(GameProfile gameProfile, SkinTextureCallback skinTextureCallback, boolean bl) {
        EXECUTOR_SERVICE.submit(() -> {
            HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();
            try {
                map.putAll(this.sessionService.getTextures(gameProfile, bl));
            } catch (InsecureTextureException insecureTextureException) {
                // empty catch block
            }
            if (map.isEmpty()) {
                gameProfile.getProperties().clear();
                if (gameProfile.getId().equals(Minecraft.getInstance().getUser().getGameProfile().getId())) {
                    gameProfile.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
                    map.putAll(this.sessionService.getTextures(gameProfile, false));
                } else {
                    this.sessionService.fillProfileProperties(gameProfile, bl);
                    try {
                        map.putAll(this.sessionService.getTextures(gameProfile, bl));
                    } catch (InsecureTextureException insecureTextureException) {
                        // empty catch block
                    }
                }
            }
            Minecraft.getInstance().execute(() -> {
                if (map.containsKey((Object)MinecraftProfileTexture.Type.SKIN)) {
                    this.registerTexture((MinecraftProfileTexture)map.get((Object)MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, skinTextureCallback);
                }
                if (map.containsKey((Object)MinecraftProfileTexture.Type.CAPE)) {
                    this.registerTexture((MinecraftProfileTexture)map.get((Object)MinecraftProfileTexture.Type.CAPE), MinecraftProfileTexture.Type.CAPE, skinTextureCallback);
                }
            });
        });
    }

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile gameProfile) {
        return this.insecureSkinCache.getUnchecked(gameProfile);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface SkinTextureCallback {
        public void onSkinTextureAvailable(MinecraftProfileTexture.Type var1, ResourceLocation var2, MinecraftProfileTexture var3);
    }
}

