/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BitmapProvider
implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
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
    public GlyphInfo getGlyph(int i) {
        return (GlyphInfo)this.glyphs.get(i);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    @Environment(value=EnvType.CLIENT)
    record Glyph(float scale, NativeImage image, int offsetX, int offsetY, int width, int height, int advance, int ascent) implements GlyphInfo
    {
        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return function.apply(new SheetGlyphInfo(){

                @Override
                public float getOversample() {
                    return 1.0f / scale;
                }

                @Override
                public int getPixelWidth() {
                    return width;
                }

                @Override
                public int getPixelHeight() {
                    return height;
                }

                @Override
                public float getBearingY() {
                    return SheetGlyphInfo.super.getBearingY() + 7.0f - (float)ascent;
                }

                @Override
                public void upload(int i, int j) {
                    image.upload(0, i, j, offsetX, offsetY, width, height, false, false);
                }

                @Override
                public boolean isColored() {
                    return image.format().components() > 1;
                }
            });
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
            this.texture = resourceLocation.withPrefix("textures/");
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
                InputStream inputStream = resourceManager.open(this.texture);
                try {
                    NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, inputStream);
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
                            if (o == 0 || (glyph = int2ObjectMap.put(o, new Glyph(f, nativeImage, p * k, m * l, k, l, (int)(0.5 + (double)((float)(q = this.getActualGlyphWidth(nativeImage, k, l, p, m)) * f)) + 1, this.ascent))) == null) continue;
                            LOGGER.warn("Codepoint '{}' declared multiple times in {}", (Object)Integer.toHexString(o), (Object)this.texture);
                        }
                    }
                    bitmapProvider = new BitmapProvider(nativeImage, int2ObjectMap);
                    if (inputStream == null) break block10;
                } catch (Throwable throwable) {
                    try {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException iOException) {
                        throw new RuntimeException(iOException.getMessage());
                    }
                }
                inputStream.close();
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

