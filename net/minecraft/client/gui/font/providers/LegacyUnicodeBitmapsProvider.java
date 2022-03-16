/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
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
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
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
    public GlyphInfo getGlyph(int i) {
        NativeImage nativeImage;
        if (i < 0 || i >= this.sizes.length) {
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
        for (int i = 0; i < this.sizes.length; ++i) {
            if (this.sizes[i] == 0) continue;
            intSet.add(i);
        }
        return intSet;
    }

    @Nullable
    private NativeImage loadTexture(ResourceLocation resourceLocation) {
        NativeImage nativeImage;
        block8: {
            Resource resource = this.resourceManager.getResource(resourceLocation);
            try {
                nativeImage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
                if (resource == null) break block8;
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
                    LOGGER.error("Couldn't load texture {}", (Object)resourceLocation, (Object)iOException);
                    return null;
                }
            }
            resource.close();
        }
        return nativeImage;
    }

    private static int getLeft(byte b) {
        return b >> 4 & 0xF;
    }

    private static int getRight(byte b) {
        return (b & 0xF) + 1;
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
                String.format(string, "");
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
                Resource resource = Minecraft.getInstance().getResourceManager().getResource(this.metadata);
                try {
                    byte[] bs = resource.getInputStream().readNBytes(65536);
                    legacyUnicodeBitmapsProvider = new LegacyUnicodeBitmapsProvider(resourceManager, bs, this.texturePattern);
                    if (resource == null) break block8;
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
                        LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", (Object)this.metadata);
                        return null;
                    }
                }
                resource.close();
            }
            return legacyUnicodeBitmapsProvider;
        }
    }
}

