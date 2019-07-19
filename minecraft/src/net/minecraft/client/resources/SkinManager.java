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
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.HttpTextureProcessor;
import net.minecraft.client.renderer.MobSkinTextureProcessor;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SkinManager {
	private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());
	private final TextureManager textureManager;
	private final File skinsDirectory;
	private final MinecraftSessionService sessionService;
	private final LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> insecureSkinCache;

	public SkinManager(TextureManager textureManager, File file, MinecraftSessionService minecraftSessionService) {
		this.textureManager = textureManager;
		this.skinsDirectory = file;
		this.sessionService = minecraftSessionService;
		this.insecureSkinCache = CacheBuilder.newBuilder()
			.expireAfterAccess(15L, TimeUnit.SECONDS)
			.build(new CacheLoader<GameProfile, Map<Type, MinecraftProfileTexture>>() {
				public Map<Type, MinecraftProfileTexture> load(GameProfile gameProfile) throws Exception {
					try {
						return Minecraft.getInstance().getMinecraftSessionService().getTextures(gameProfile, false);
					} catch (Throwable var3) {
						return Maps.<Type, MinecraftProfileTexture>newHashMap();
					}
				}
			});
	}

	public ResourceLocation registerTexture(MinecraftProfileTexture minecraftProfileTexture, Type type) {
		return this.registerTexture(minecraftProfileTexture, type, null);
	}

	public ResourceLocation registerTexture(
		MinecraftProfileTexture minecraftProfileTexture, Type type, @Nullable SkinManager.SkinTextureCallback skinTextureCallback
	) {
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
			final HttpTextureProcessor httpTextureProcessor = type == Type.SKIN ? new MobSkinTextureProcessor() : null;
			HttpTexture httpTexture = new HttpTexture(file2, minecraftProfileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), new HttpTextureProcessor() {
				@Override
				public NativeImage process(NativeImage nativeImage) {
					return httpTextureProcessor != null ? httpTextureProcessor.process(nativeImage) : nativeImage;
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

	public void registerSkins(GameProfile gameProfile, SkinManager.SkinTextureCallback skinTextureCallback, boolean bl) {
		EXECUTOR_SERVICE.submit(() -> {
			Map<Type, MinecraftProfileTexture> map = Maps.<Type, MinecraftProfileTexture>newHashMap();

			try {
				map.putAll(this.sessionService.getTextures(gameProfile, bl));
			} catch (InsecureTextureException var7) {
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
					} catch (InsecureTextureException var6) {
					}
				}
			}

			Minecraft.getInstance().execute(() -> {
				if (map.containsKey(Type.SKIN)) {
					this.registerTexture((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN, skinTextureCallback);
				}

				if (map.containsKey(Type.CAPE)) {
					this.registerTexture((MinecraftProfileTexture)map.get(Type.CAPE), Type.CAPE, skinTextureCallback);
				}
			});
		});
	}

	public Map<Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile gameProfile) {
		return this.insecureSkinCache.getUnchecked(gameProfile);
	}

	@Environment(EnvType.CLIENT)
	public interface SkinTextureCallback {
		void onSkinTextureAvailable(Type type, ResourceLocation resourceLocation, MinecraftProfileTexture minecraftProfileTexture);
	}
}
