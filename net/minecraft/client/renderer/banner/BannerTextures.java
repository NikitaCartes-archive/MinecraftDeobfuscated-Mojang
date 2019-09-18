/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.banner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BannerTextures {
    public static final TextureCache SHIELD_CACHE = new TextureCache("shield_", new ResourceLocation("textures/entity/shield_base.png"), "textures/entity/shield/");
    public static final ResourceLocation NO_PATTERN_SHIELD = new ResourceLocation("textures/entity/shield_base_nopattern.png");
    public static final ResourceLocation DEFAULT_PATTERN_BANNER = new ResourceLocation("textures/entity/banner/base.png");

    @Environment(value=EnvType.CLIENT)
    static class TimestampedBannerTexture {
        public long lastUseMilliseconds;
        public ResourceLocation textureLocation;

        private TimestampedBannerTexture() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TextureCache {
        private final Map<String, TimestampedBannerTexture> cache = Maps.newLinkedHashMap();
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
            }
            if (list.isEmpty() || list2.isEmpty()) {
                return MissingTextureAtlasSprite.getLocation();
            }
            string = this.hashPrefix + string;
            TimestampedBannerTexture timestampedBannerTexture = this.cache.get(string);
            if (timestampedBannerTexture == null) {
                if (this.cache.size() >= 256 && !this.freeCacheSlot()) {
                    return DEFAULT_PATTERN_BANNER;
                }
                ArrayList<String> list3 = Lists.newArrayList();
                for (BannerPattern bannerPattern : list) {
                    list3.add(this.resourceNameBase + bannerPattern.getFilename() + ".png");
                }
                timestampedBannerTexture = new TimestampedBannerTexture();
                timestampedBannerTexture.textureLocation = new ResourceLocation(string);
                Minecraft.getInstance().getTextureManager().register(timestampedBannerTexture.textureLocation, new LayeredColorMaskTexture(this.baseResource, list3, list2));
                this.cache.put(string, timestampedBannerTexture);
            }
            timestampedBannerTexture.lastUseMilliseconds = Util.getMillis();
            return timestampedBannerTexture.textureLocation;
        }

        private boolean freeCacheSlot() {
            long l = Util.getMillis();
            Iterator<String> iterator = this.cache.keySet().iterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                TimestampedBannerTexture timestampedBannerTexture = this.cache.get(string);
                if (l - timestampedBannerTexture.lastUseMilliseconds <= 5000L) continue;
                Minecraft.getInstance().getTextureManager().release(timestampedBannerTexture.textureLocation);
                iterator.remove();
                return true;
            }
            return this.cache.size() < 256;
        }
    }
}

