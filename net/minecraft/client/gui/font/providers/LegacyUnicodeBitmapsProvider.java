/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LegacyUnicodeBitmapsProvider
implements GlyphProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int UNICODE_SHEETS = 256;
    private static final int CHARS_PER_SHEET = 256;
    private static final int TEXTURE_SIZE = 256;
    private final ResourceManager resourceManager;
    private final byte[] sizes;
    private final String texturePattern;
    private final Map<ResourceLocation, NativeImage> textures = Maps.newHashMap();

    public LegacyUnicodeBitmapsProvider(ResourceManager resourceManager, byte[] bs, String string) {
        this.resourceManager = resourceManager;
        this.sizes = bs;
        this.texturePattern = string;
        for (int i = 0; i < 256; ++i) {
            int j = i * 256;
            ResourceLocation resourceLocation = this.getSheetLocation(j);
            try (Resource resource = this.resourceManager.getResource(resourceLocation);
                 NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());){
                if (nativeImage.getWidth() == 256 && nativeImage.getHeight() == 256) {
                    for (int k = 0; k < 256; ++k) {
                        byte b = bs[j + k];
                        if (b == 0 || LegacyUnicodeBitmapsProvider.getLeft(b) <= LegacyUnicodeBitmapsProvider.getRight(b)) continue;
                        bs[j + k] = 0;
                    }
                    continue;
                }
            } catch (IOException iOException) {
                // empty catch block
            }
            Arrays.fill(bs, j, j + 256, (byte)0);
        }
    }

    @Override
    public void close() {
        this.textures.values().forEach(NativeImage::close);
    }

    private ResourceLocation getSheetLocation(int i) {
        ResourceLocation resourceLocation = new ResourceLocation(String.format(this.texturePattern, String.format("%02x", i / 256)));
        return new ResourceLocation(resourceLocation.getNamespace(), "textures/" + resourceLocation.getPath());
    }

    @Override
    @Nullable
    public RawGlyph getGlyph(int i) {
        NativeImage nativeImage;
        if (i < 0 || i > 65535) {
            return null;
        }
        byte b = this.sizes[i];
        if (b != 0 && (nativeImage = this.textures.computeIfAbsent(this.getSheetLocation(i), this::loadTexture)) != null) {
            int j = LegacyUnicodeBitmapsProvider.getLeft(b);
            return new Glyph(i % 16 * 16 + j, (i & 0xFF) / 16 * 16, LegacyUnicodeBitmapsProvider.getRight(b) - j, 16, nativeImage);
        }
        return null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (int i = 0; i < 65535; ++i) {
            if (this.sizes[i] == 0) continue;
            intSet.add(i);
        }
        return intSet;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private NativeImage loadTexture(ResourceLocation resourceLocation) {
        try (Resource resource = this.resourceManager.getResource(resourceLocation);){
            NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
            return nativeImage;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't load texture {}", (Object)resourceLocation, (Object)iOException);
            return null;
        }
    }

    private static int getLeft(byte b) {
        return b >> 4 & 0xF;
    }

    private static int getRight(byte b) {
        return (b & 0xF) + 1;
    }

    @Environment(value=EnvType.CLIENT)
    static class Glyph
    implements RawGlyph {
        private final int width;
        private final int height;
        private final int sourceX;
        private final int sourceY;
        private final NativeImage source;

        private Glyph(int i, int j, int k, int l, NativeImage nativeImage) {
            this.width = k;
            this.height = l;
            this.sourceX = i;
            this.sourceY = j;
            this.source = nativeImage;
        }

        @Override
        public float getOversample() {
            return 2.0f;
        }

        @Override
        public int getPixelWidth() {
            return this.width;
        }

        @Override
        public int getPixelHeight() {
            return this.height;
        }

        @Override
        public float getAdvance() {
            return this.width / 2 + 1;
        }

        @Override
        public void upload(int i, int j) {
            this.source.upload(0, i, j, this.sourceX, this.sourceY, this.width, this.height, false, false);
        }

        @Override
        public boolean isColored() {
            return this.source.format().components() > 1;
        }

        @Override
        public float getShadowOffset() {
            return 0.5f;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder
    implements GlyphProviderBuilder {
        private final ResourceLocation metadata;
        private final String texturePattern;

        public Builder(ResourceLocation resourceLocation, String string) {
            this.metadata = resourceLocation;
            this.texturePattern = string;
        }

        public static GlyphProviderBuilder fromJson(JsonObject jsonObject) {
            return new Builder(new ResourceLocation(GsonHelper.getAsString(jsonObject, "sizes")), GsonHelper.getAsString(jsonObject, "template"));
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        @Nullable
        public GlyphProvider create(ResourceManager resourceManager) {
            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(this.metadata);){
                byte[] bs = new byte[65536];
                resource.getInputStream().read(bs);
                LegacyUnicodeBitmapsProvider legacyUnicodeBitmapsProvider = new LegacyUnicodeBitmapsProvider(resourceManager, bs, this.texturePattern);
                return legacyUnicodeBitmapsProvider;
            } catch (IOException iOException) {
                LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", (Object)this.metadata);
                return null;
            }
        }
    }
}

