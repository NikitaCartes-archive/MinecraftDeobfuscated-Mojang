package net.minecraft.client.renderer.banner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;

@Environment(EnvType.CLIENT)
public class BannerTextures {
	public static final BannerTextures.TextureCache SHIELD_CACHE = new BannerTextures.TextureCache(
		"shield_", new ResourceLocation("textures/entity/shield_base.png"), "textures/entity/shield/"
	);
	public static final ResourceLocation NO_PATTERN_SHIELD = new ResourceLocation("textures/entity/shield_base_nopattern.png");
	public static final ResourceLocation DEFAULT_PATTERN_BANNER = new ResourceLocation("textures/entity/banner/base.png");

	@Environment(EnvType.CLIENT)
	public static class TextureCache {
		private final Map<String, BannerTextures.TimestampedBannerTexture> cache = Maps.<String, BannerTextures.TimestampedBannerTexture>newLinkedHashMap();
		private final ResourceLocation baseResource;
		private final String resourceNameBase;
		private final String hashPrefix;

		public TextureCache(String string, ResourceLocation resourceLocation, String string2) {
			this.hashPrefix = string;
			this.baseResource = resourceLocation;
			this.resourceNameBase = string2;
		}

		@Nullable
		public ResourceLocation getTextureLocation(String string, List<BannerPattern> list, List<DyeColor> list2) {
			if (string.isEmpty()) {
				return null;
			} else if (!list.isEmpty() && !list2.isEmpty()) {
				string = this.hashPrefix + string;
				BannerTextures.TimestampedBannerTexture timestampedBannerTexture = (BannerTextures.TimestampedBannerTexture)this.cache.get(string);
				if (timestampedBannerTexture == null) {
					if (this.cache.size() >= 256 && !this.freeCacheSlot()) {
						return BannerTextures.DEFAULT_PATTERN_BANNER;
					}

					List<String> list3 = Lists.<String>newArrayList();

					for (BannerPattern bannerPattern : list) {
						list3.add(this.resourceNameBase + bannerPattern.getFilename() + ".png");
					}

					timestampedBannerTexture = new BannerTextures.TimestampedBannerTexture();
					timestampedBannerTexture.textureLocation = new ResourceLocation(string);
					Minecraft.getInstance()
						.getTextureManager()
						.register(timestampedBannerTexture.textureLocation, new LayeredColorMaskTexture(this.baseResource, list3, list2));
					this.cache.put(string, timestampedBannerTexture);
				}

				timestampedBannerTexture.lastUseMilliseconds = Util.getMillis();
				return timestampedBannerTexture.textureLocation;
			} else {
				return MissingTextureAtlasSprite.getLocation();
			}
		}

		private boolean freeCacheSlot() {
			long l = Util.getMillis();
			Iterator<String> iterator = this.cache.keySet().iterator();

			while (iterator.hasNext()) {
				String string = (String)iterator.next();
				BannerTextures.TimestampedBannerTexture timestampedBannerTexture = (BannerTextures.TimestampedBannerTexture)this.cache.get(string);
				if (l - timestampedBannerTexture.lastUseMilliseconds > 5000L) {
					Minecraft.getInstance().getTextureManager().release(timestampedBannerTexture.textureLocation);
					iterator.remove();
					return true;
				}
			}

			return this.cache.size() < 256;
		}
	}

	@Environment(EnvType.CLIENT)
	static class TimestampedBannerTexture {
		public long lastUseMilliseconds;
		public ResourceLocation textureLocation;

		private TimestampedBannerTexture() {
		}
	}
}
