package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SkinManager {
	private final TextureManager textureManager;
	private final File skinsDirectory;
	private final MinecraftSessionService sessionService;
	private final LoadingCache<String, Map<Type, MinecraftProfileTexture>> insecureSkinCache;

	public SkinManager(TextureManager textureManager, File file, MinecraftSessionService minecraftSessionService) {
		this.textureManager = textureManager;
		this.skinsDirectory = file;
		this.sessionService = minecraftSessionService;
		this.insecureSkinCache = CacheBuilder.newBuilder()
			.expireAfterAccess(15L, TimeUnit.SECONDS)
			.build(new CacheLoader<String, Map<Type, MinecraftProfileTexture>>() {
				public Map<Type, MinecraftProfileTexture> load(String string) {
					GameProfile gameProfile = new GameProfile(null, "dummy_mcdummyface");
					gameProfile.getProperties().put("textures", new Property("textures", string, ""));

					try {
						return minecraftSessionService.getTextures(gameProfile, false);
					} catch (Throwable var4) {
						return ImmutableMap.of();
					}
				}
			});
	}

	public ResourceLocation registerTexture(MinecraftProfileTexture minecraftProfileTexture, Type type) {
		return this.registerTexture(minecraftProfileTexture, type, null);
	}

	private ResourceLocation registerTexture(
		MinecraftProfileTexture minecraftProfileTexture, Type type, @Nullable SkinManager.SkinTextureCallback skinTextureCallback
	) {
		String string = Hashing.sha1().hashUnencodedChars(minecraftProfileTexture.getHash()).toString();
		ResourceLocation resourceLocation = new ResourceLocation("skins/" + string);
		AbstractTexture abstractTexture = this.textureManager.getTexture(resourceLocation, MissingTextureAtlasSprite.getTexture());
		if (abstractTexture == MissingTextureAtlasSprite.getTexture()) {
			File file = new File(this.skinsDirectory, string.length() > 2 ? string.substring(0, 2) : "xx");
			File file2 = new File(file, string);
			HttpTexture httpTexture = new HttpTexture(file2, minecraftProfileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), type == Type.SKIN, () -> {
				if (skinTextureCallback != null) {
					skinTextureCallback.onSkinTextureAvailable(type, resourceLocation, minecraftProfileTexture);
				}
			});
			this.textureManager.register(resourceLocation, httpTexture);
		} else if (skinTextureCallback != null) {
			skinTextureCallback.onSkinTextureAvailable(type, resourceLocation, minecraftProfileTexture);
		}

		return resourceLocation;
	}

	public void registerSkins(GameProfile gameProfile, SkinManager.SkinTextureCallback skinTextureCallback, boolean bl) {
		Runnable runnable = () -> {
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

			Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(() -> ImmutableList.of(Type.SKIN, Type.CAPE).forEach(type -> {
						if (map.containsKey(type)) {
							this.registerTexture((MinecraftProfileTexture)map.get(type), type, skinTextureCallback);
						}
					})));
		};
		Util.backgroundExecutor().execute(runnable);
	}

	public Map<Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile gameProfile) {
		Property property = Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
		return (Map<Type, MinecraftProfileTexture>)(property == null ? ImmutableMap.of() : this.insecureSkinCache.getUnchecked(property.getValue()));
	}

	@Environment(EnvType.CLIENT)
	public interface SkinTextureCallback {
		void onSkinTextureAvailable(Type type, ResourceLocation resourceLocation, MinecraftProfileTexture minecraftProfileTexture);
	}
}
