/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BitmapProvider
implements GlyphProvider {
    static final Logger LOGGER = LogManager.getLogger();
    private final NativeImage image;
    private final Int2ObjectMap<Glyph> glyphs;

    BitmapProvider(NativeImage nativeImage, Int2ObjectMap<Glyph> int2ObjectMap) {
        this.image = nativeImage;
        this.glyphs = int2ObjectMap;
    }

    @Override
    public void close() {
        this.image.close();
    }

    @Override
    @Nullable
    public RawGlyph getGlyph(int i) {
        return (RawGlyph)this.glyphs.get(i);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    @Environment(value=EnvType.CLIENT)
    static final class Glyph
    implements RawGlyph {
        private final float scale;
        private final NativeImage image;
        private final int offsetX;
        private final int offsetY;
        private final int width;
        private final int height;
        private final int advance;
        private final int ascent;

        Glyph(float f, NativeImage nativeImage, int i, int j, int k, int l, int m, int n) {
            this.scale = f;
            this.image = nativeImage;
            this.offsetX = i;
            this.offsetY = j;
            this.width = k;
            this.height = l;
            this.advance = m;
            this.ascent = n;
        }

        @Override
        public float getOversample() {
            return 1.0f / this.scale;
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
            return this.advance;
        }

        @Override
        public float getBearingY() {
            return RawGlyph.super.getBearingY() + 7.0f - (float)this.ascent;
        }

        @Override
        public void upload(int i, int j) {
            this.image.upload(0, i, j, this.offsetX, this.offsetY, this.width, this.height, false, false);
        }

        @Override
        public boolean isColored() {
            return this.image.format().components() > 1;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder
    implements GlyphProviderBuilder {
        private final ResourceLocation texture;
        private final List<int[]> chars;
        private final int height;
        private final int ascent;

        public Builder(ResourceLocation resourceLocation, int i, int j, List<int[]> list) {
            this.texture = new ResourceLocation(resourceLocation.getNamespace(), "textures/" + resourceLocation.getPath());
            this.chars = list;
            this.height = i;
            this.ascent = j;
        }

        public static Builder fromJson(JsonObject jsonObject) {
            int i = GsonHelper.getAsInt(jsonObject, "height", 8);
            int j = GsonHelper.getAsInt(jsonObject, "ascent");
            if (j > i) {
                throw new JsonParseException("Ascent " + j + " higher than height " + i);
            }
            ArrayList<int[]> list = Lists.newArrayList();
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "chars");
            for (int k = 0; k < jsonArray.size(); ++k) {
                int l;
                String string = GsonHelper.convertToString(jsonArray.get(k), "chars[" + k + "]");
                int[] is = string.codePoints().toArray();
                if (k > 0 && is.length != (l = ((int[])list.get(0)).length)) {
                    throw new JsonParseException("Elements of chars have to be the same length (found: " + is.length + ", expected: " + l + "), pad with space or \\u0000");
                }
                list.add(is);
            }
            if (list.isEmpty() || ((int[])list.get(0)).length == 0) {
                throw new JsonParseException("Expected to find data in chars, found none.");
            }
            return new Builder(new ResourceLocation(GsonHelper.getAsString(jsonObject, "file")), i, j, list);
        }

        @Override
        @Nullable
        public GlyphProvider create(ResourceManager resourceManager) {
            BitmapProvider bitmapProvider;
            block10: {
                Resource resource = resourceManager.getResource(this.texture);
                try {
                    NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
                    int i = nativeImage.getWidth();
                    int j = nativeImage.getHeight();
                    int k = i / this.chars.get(0).length;
                    int l = j / this.chars.size();
                    float f = (float)this.height / (float)l;
                    Int2ObjectOpenHashMap<Glyph> int2ObjectMap = new Int2ObjectOpenHashMap<Glyph>();
                    for (int m = 0; m < this.chars.size(); ++m) {
                        int n = 0;
                        for (int o : this.chars.get(m)) {
                            int q;
                            Glyph glyph;
                            int p = n++;
                            if (o == 0 || o == 32 || (glyph = int2ObjectMap.put(o, new Glyph(f, nativeImage, p * k, m * l, k, l, (int)(0.5 + (double)((float)(q = this.getActualGlyphWidth(nativeImage, k, l, p, m)) * f)) + 1, this.ascent))) == null) continue;
                            LOGGER.warn("Codepoint '{}' declared multiple times in {}", (Object)Integer.toHexString(o), (Object)this.texture);
                        }
                    }
                    bitmapProvider = new BitmapProvider(nativeImage, int2ObjectMap);
                    if (resource == null) break block10;
                } catch (Throwable throwable) {
                    try {
                        if (resource != null) {
                            try {
                                resource.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException iOException) {
                        throw new RuntimeException(iOException.getMessage());
                    }
                }
                resource.close();
            }
            return bitmapProvider;
        }

        private int getActualGlyphWidth(NativeImage nativeImage, int i, int j, int k, int l) {
            int m;
            for (m = i - 1; m >= 0; --m) {
                int n = k * i + m;
                for (int o = 0; o < j; ++o) {
                    int p = l * j + o;
                    if (nativeImage.getLuminanceOrAlpha(n, p) == 0) continue;
                    return m + 1;
                }
            }
            return m + 1;
        }
    }
}

