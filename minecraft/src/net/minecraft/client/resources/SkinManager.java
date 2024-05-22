package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SkinManager {
	static final Logger LOGGER = LogUtils.getLogger();
	private final MinecraftSessionService sessionService;
	private final LoadingCache<SkinManager.CacheKey, CompletableFuture<PlayerSkin>> skinCache;
	private final SkinManager.TextureCache skinTextures;
	private final SkinManager.TextureCache capeTextures;
	private final SkinManager.TextureCache elytraTextures;

	public SkinManager(TextureManager textureManager, Path path, MinecraftSessionService minecraftSessionService, Executor executor) {
		this.sessionService = minecraftSessionService;
		this.skinTextures = new SkinManager.TextureCache(textureManager, path, Type.SKIN);
		this.capeTextures = new SkinManager.TextureCache(textureManager, path, Type.CAPE);
		this.elytraTextures = new SkinManager.TextureCache(textureManager, path, Type.ELYTRA);
		this.skinCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofSeconds(15L))
			.build(
				new CacheLoader<SkinManager.CacheKey, CompletableFuture<PlayerSkin>>() {
					public CompletableFuture<PlayerSkin> load(SkinManager.CacheKey cacheKey) {
						return CompletableFuture.supplyAsync(() -> {
								Property property = cacheKey.packedTextures();
								if (property == null) {
									return MinecraftProfileTextures.EMPTY;
								} else {
									MinecraftProfileTextures minecraftProfileTextures = minecraftSessionService.unpackTextures(property);
									if (minecraftProfileTextures.signatureState() == SignatureState.INVALID) {
										SkinManager.LOGGER.warn("Profile contained invalid signature for textures property (profile id: {})", cacheKey.profileId());
									}

									return minecraftProfileTextures;
								}
							}, Util.backgroundExecutor())
							.thenComposeAsync(minecraftProfileTextures -> SkinManager.this.registerTextures(cacheKey.profileId(), minecraftProfileTextures), executor);
					}
				}
			);
	}

	public Supplier<PlayerSkin> lookupInsecure(GameProfile gameProfile) {
		CompletableFuture<PlayerSkin> completableFuture = this.getOrLoad(gameProfile);
		PlayerSkin playerSkin = DefaultPlayerSkin.get(gameProfile);
		return () -> (PlayerSkin)completableFuture.getNow(playerSkin);
	}

	public PlayerSkin getInsecureSkin(GameProfile gameProfile) {
		PlayerSkin playerSkin = (PlayerSkin)this.getOrLoad(gameProfile).getNow(null);
		return playerSkin != null ? playerSkin : DefaultPlayerSkin.get(gameProfile);
	}

	public CompletableFuture<PlayerSkin> getOrLoad(GameProfile gameProfile) {
		Property property = this.sessionService.getPackedTextures(gameProfile);
		return this.skinCache.getUnchecked(new SkinManager.CacheKey(gameProfile.getId(), property));
	}

	CompletableFuture<PlayerSkin> registerTextures(UUID uUID, MinecraftProfileTextures minecraftProfileTextures) {
		MinecraftProfileTexture minecraftProfileTexture = minecraftProfileTextures.skin();
		CompletableFuture<ResourceLocation> completableFuture;
		PlayerSkin.Model model;
		if (minecraftProfileTexture != null) {
			completableFuture = this.skinTextures.getOrLoad(minecraftProfileTexture);
			model = PlayerSkin.Model.byName(minecraftProfileTexture.getMetadata("model"));
		} else {
			PlayerSkin playerSkin = DefaultPlayerSkin.get(uUID);
			completableFuture = CompletableFuture.completedFuture(playerSkin.texture());
			model = playerSkin.model();
		}

		String string = Optionull.map(minecraftProfileTexture, MinecraftProfileTexture::getUrl);
		MinecraftProfileTexture minecraftProfileTexture2 = minecraftProfileTextures.cape();
		CompletableFuture<ResourceLocation> completableFuture2 = minecraftProfileTexture2 != null
			? this.capeTextures.getOrLoad(minecraftProfileTexture2)
			: CompletableFuture.completedFuture(null);
		MinecraftProfileTexture minecraftProfileTexture3 = minecraftProfileTextures.elytra();
		CompletableFuture<ResourceLocation> completableFuture3 = minecraftProfileTexture3 != null
			? this.elytraTextures.getOrLoad(minecraftProfileTexture3)
			: CompletableFuture.completedFuture(null);
		return CompletableFuture.allOf(completableFuture, completableFuture2, completableFuture3)
			.thenApply(
				void_ -> new PlayerSkin(
						(ResourceLocation)completableFuture.join(),
						string,
						(ResourceLocation)completableFuture2.join(),
						(ResourceLocation)completableFuture3.join(),
						model,
						minecraftProfileTextures.signatureState() == SignatureState.SIGNED
					)
			);
	}

	@Environment(EnvType.CLIENT)
	static record CacheKey(UUID profileId, @Nullable Property packedTextures) {
	}

	@Environment(EnvType.CLIENT)
	static class TextureCache {
		private final TextureManager textureManager;
		private final Path root;
		private final Type type;
		private final Map<String, CompletableFuture<ResourceLocation>> textures = new Object2ObjectOpenHashMap<>();

		TextureCache(TextureManager textureManager, Path path, Type type) {
			this.textureManager = textureManager;
			this.root = path;
			this.type = type;
		}

		public CompletableFuture<ResourceLocation> getOrLoad(MinecraftProfileTexture minecraftProfileTexture) {
			String string = minecraftProfileTexture.getHash();
			CompletableFuture<ResourceLocation> completableFuture = (CompletableFuture<ResourceLocation>)this.textures.get(string);
			if (completableFuture == null) {
				completableFuture = this.registerTexture(minecraftProfileTexture);
				this.textures.put(string, completableFuture);
			}

			return completableFuture;
		}

		private CompletableFuture<ResourceLocation> registerTexture(MinecraftProfileTexture minecraftProfileTexture) {
			String string = Hashing.sha1().hashUnencodedChars(minecraftProfileTexture.getHash()).toString();
			ResourceLocation resourceLocation = this.getTextureLocation(string);
			Path path = this.root.resolve(string.length() > 2 ? string.substring(0, 2) : "xx").resolve(string);
			CompletableFuture<ResourceLocation> completableFuture = new CompletableFuture();
			HttpTexture httpTexture = new HttpTexture(
				path.toFile(),
				minecraftProfileTexture.getUrl(),
				DefaultPlayerSkin.getDefaultTexture(),
				this.type == Type.SKIN,
				() -> completableFuture.complete(resourceLocation)
			);
			this.textureManager.register(resourceLocation, httpTexture);
			return completableFuture;
		}

		private ResourceLocation getTextureLocation(String string) {
			String string2 = switch (this.type) {
				case SKIN -> "skins";
				case CAPE -> "capes";
				case ELYTRA -> "elytra";
			};
			return ResourceLocation.withDefaultNamespace(string2 + "/" + string);
		}
	}
}
