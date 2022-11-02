/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LegacyUnicodeBitmapsProvider
implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int UNICODE_SHEETS = 256;
    private static final int CODEPOINTS_PER_SHEET = 256;
    private static final int TEXTURE_SIZE = 256;
    private static final byte NO_GLYPH = 0;
    private static final int TOTAL_CODEPOINTS = 65536;
    private final byte[] sizes;
    private final Sheet[] sheets = new Sheet[256];

    public LegacyUnicodeBitmapsProvider(ResourceManager resourceManager, byte[] bs, String string) {
        this.sizes = bs;
        HashSet<ResourceLocation> set = new HashSet<ResourceLocation>();
        for (int i = 0; i < 256; ++i) {
            int j = i * 256;
            set.add(LegacyUnicodeBitmapsProvider.getSheetLocation(string, j));
        }
        String string2 = LegacyUnicodeBitmapsProvider.getCommonSearchPrefix(set);
        HashMap map = new HashMap();
        resourceManager.listResources(string2, set::contains).forEach((resourceLocation, resource) -> map.put(resourceLocation, CompletableFuture.supplyAsync(() -> {
            NativeImage nativeImage;
            block8: {
                InputStream inputStream = resource.open();
                try {
                    nativeImage = NativeImage.read(NativeImage.Format.RGBA, inputStream);
                    if (inputStream == null) break block8;
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
                        LOGGER.error("Failed to read resource {} from pack {}", resourceLocation, (Object)resource.sourcePackId());
                        return null;
                    }
                }
                inputStream.close();
            }
            return nativeImage;
        }, Util.backgroundExecutor())));
        ArrayList<CompletionStage> list = new ArrayList<CompletionStage>(256);
        for (int k = 0; k < 256; ++k) {
            int l = k * 256;
            int m = k;
            ResourceLocation resourceLocation2 = LegacyUnicodeBitmapsProvider.getSheetLocation(string, l);
            CompletableFuture completableFuture = (CompletableFuture)map.get(resourceLocation2);
            if (completableFuture == null) continue;
            list.add(completableFuture.thenAcceptAsync(nativeImage -> {
                if (nativeImage == null) {
                    return;
                }
                if (nativeImage.getWidth() == 256 && nativeImage.getHeight() == 256) {
                    for (int k = 0; k < 256; ++k) {
                        byte b = bs[l + k];
                        if (b == 0 || LegacyUnicodeBitmapsProvider.getLeft(b) <= LegacyUnicodeBitmapsProvider.getRight(b)) continue;
                        bs[i + k] = 0;
                    }
                    this.sheets[j] = new Sheet(bs, (NativeImage)nativeImage);
                } else {
                    nativeImage.close();
                    Arrays.fill(bs, l, l + 256, (byte)0);
                }
            }, (Executor)Util.backgroundExecutor()));
        }
        CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new)).join();
    }

    private static String getCommonSearchPrefix(Set<ResourceLocation> set) {
        String string = StringUtils.getCommonPrefix((String[])set.stream().map(ResourceLocation::getPath).toArray(String[]::new));
        int i = string.lastIndexOf("/");
        if (i == -1) {
            return "";
        }
        return string.substring(0, i);
    }

    @Override
    public void close() {
        for (Sheet sheet : this.sheets) {
            if (sheet == null) continue;
            sheet.close();
        }
    }

    private static ResourceLocation getSheetLocation(String string, int i) {
        String string2 = String.format(Locale.ROOT, "%02x", i / 256);
        ResourceLocation resourceLocation = new ResourceLocation(String.format(Locale.ROOT, string, string2));
        return resourceLocation.withPrefix("textures/");
    }

    @Override
    @Nullable
    public GlyphInfo getGlyph(int i) {
        if (i < 0 || i >= this.sizes.length) {
            return null;
        }
        int j = i / 256;
        Sheet sheet = this.sheets[j];
        return sheet != null ? sheet.getGlyph(i) : null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (int i = 0; i < this.sizes.length; ++i) {
            if (this.sizes[i] == 0) continue;
            intSet.add(i);
        }
        return intSet;
    }

    static int getLeft(byte b) {
        return b >> 4 & 0xF;
    }

    static int getRight(byte b) {
        return (b & 0xF) + 1;
    }

    @Environment(value=EnvType.CLIENT)
    static class Sheet
    implements AutoCloseable {
        private final byte[] sizes;
        private final NativeImage source;

        Sheet(byte[] bs, NativeImage nativeImage) {
            this.sizes = bs;
            this.source = nativeImage;
        }

        @Override
        public void close() {
            this.source.close();
        }

        @Nullable
        public GlyphInfo getGlyph(int i) {
            byte b = this.sizes[i];
            if (b != 0) {
                int j = LegacyUnicodeBitmapsProvider.getLeft(b);
                return new Glyph(i % 16 * 16 + j, (i & 0xFF) / 16 * 16, LegacyUnicodeBitmapsProvider.getRight(b) - j, 16, this.source);
            }
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Glyph(int sourceX, int sourceY, int width, int height, NativeImage source) implements GlyphInfo
    {
        @Override
        public float getAdvance() {
            return this.width / 2 + 1;
        }

        @Override
        public float getShadowOffset() {
            return 0.5f;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return function.apply(new SheetGlyphInfo(){

                @Override
                public float getOversample() {
                    return 2.0f;
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
                public void upload(int i, int j) {
                    source.upload(0, i, j, sourceX, sourceY, width, height, false, false);
                }

                @Override
                public boolean isColored() {
                    return source.format().components() > 1;
                }
            });
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
            return new Builder(new ResourceLocation(GsonHelper.getAsString(jsonObject, "sizes")), Builder.getTemplate(jsonObject));
        }

        private static String getTemplate(JsonObject jsonObject) {
            String string = GsonHelper.getAsString(jsonObject, "template");
            try {
                String.format(Locale.ROOT, string, "");
            } catch (IllegalFormatException illegalFormatException) {
                throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + string);
            }
            return string;
        }

        @Override
        @Nullable
        public GlyphProvider create(ResourceManager resourceManager) {
            LegacyUnicodeBitmapsProvider legacyUnicodeBitmapsProvider;
            block8: {
                InputStream inputStream = Minecraft.getInstance().getResourceManager().open(this.metadata);
                try {
                    byte[] bs = inputStream.readNBytes(65536);
                    legacyUnicodeBitmapsProvider = new LegacyUnicodeBitmapsProvider(resourceManager, bs, this.texturePattern);
                    if (inputStream == null) break block8;
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
                        LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", (Object)this.metadata);
                        return null;
                    }
                }
                inputStream.close();
            }
            return legacyUnicodeBitmapsProvider;
        }
    }
}

