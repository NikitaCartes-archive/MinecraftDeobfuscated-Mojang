/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PalettedPermutations
implements SpriteSource {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<PalettedPermutations> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.list(ResourceLocation.CODEC).fieldOf("textures")).forGetter(palettedPermutations -> palettedPermutations.textures), ((MapCodec)ResourceLocation.CODEC.fieldOf("palette_key")).forGetter(palettedPermutations -> palettedPermutations.paletteKey), ((MapCodec)Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("permutations")).forGetter(palettedPermutations -> palettedPermutations.permutations)).apply((Applicative<PalettedPermutations, ?>)instance, PalettedPermutations::new));
    private final List<ResourceLocation> textures;
    private final Map<String, ResourceLocation> permutations;
    private final ResourceLocation paletteKey;

    private PalettedPermutations(List<ResourceLocation> list, ResourceLocation resourceLocation, Map<String, ResourceLocation> map) {
        this.textures = list;
        this.permutations = map;
        this.paletteKey = resourceLocation;
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        Supplier<int[]> supplier = Suppliers.memoize(() -> PalettedPermutations.loadPaletteEntryFromImage(resourceManager, this.paletteKey));
        HashMap map = new HashMap();
        this.permutations.forEach((string, resourceLocation) -> map.put(string, Suppliers.memoize(() -> PalettedPermutations.method_48491((java.util.function.Supplier)supplier, resourceManager, resourceLocation))));
        for (ResourceLocation resourceLocation2 : this.textures) {
            ResourceLocation resourceLocation22 = TEXTURE_ID_CONVERTER.idToFile(resourceLocation2);
            Optional<Resource> optional = resourceManager.getResource(resourceLocation22);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find texture {}", (Object)resourceLocation22);
                continue;
            }
            LazyLoadedImage lazyLoadedImage = new LazyLoadedImage(resourceLocation22, optional.get(), map.size());
            for (Map.Entry entry : map.entrySet()) {
                ResourceLocation resourceLocation3 = resourceLocation2.withSuffix("_" + (String)entry.getKey());
                output.add(resourceLocation3, new PalettedSpriteSupplier(lazyLoadedImage, (java.util.function.Supplier)entry.getValue(), resourceLocation3));
            }
        }
    }

    private static IntUnaryOperator createPaletteMapping(int[] is, int[] js) {
        if (js.length != is.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", (Object)is.length, (Object)js.length);
            throw new IllegalArgumentException();
        }
        Int2IntOpenHashMap int2IntMap = new Int2IntOpenHashMap(js.length);
        for (int i2 = 0; i2 < is.length; ++i2) {
            int j = is[i2];
            if (FastColor.ABGR32.alpha(j) == 0) continue;
            int2IntMap.put(FastColor.ABGR32.bgr(j), js[i2]);
        }
        return i -> {
            int j = FastColor.ABGR32.alpha(i);
            if (j == 0) {
                return i;
            }
            int k = FastColor.ABGR32.bgr(i);
            int l = int2IntMap.getOrDefault(k, k);
            int m = FastColor.ABGR32.alpha(l);
            return FastColor.ABGR32.color(j * m / 255, l);
        };
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static int[] loadPaletteEntryFromImage(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        Optional<Resource> optional = resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(resourceLocation));
        if (optional.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", (Object)resourceLocation);
            throw new IllegalArgumentException();
        }
        try (InputStream inputStream = optional.get().open();){
            NativeImage nativeImage = NativeImage.read(inputStream);
            try {
                int[] nArray = nativeImage.getPixelsRGBA();
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return nArray;
            } catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't load texture {}", (Object)resourceLocation, (Object)exception);
            throw new IllegalArgumentException();
        }
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.PALETTED_PERMUTATIONS;
    }

    private static /* synthetic */ IntUnaryOperator method_48491(java.util.function.Supplier supplier, ResourceManager resourceManager, ResourceLocation resourceLocation) {
        return PalettedPermutations.createPaletteMapping((int[])supplier.get(), PalettedPermutations.loadPaletteEntryFromImage(resourceManager, resourceLocation));
    }

    @Environment(value=EnvType.CLIENT)
    record PalettedSpriteSupplier(LazyLoadedImage baseImage, java.util.function.Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation) implements SpriteSource.SpriteSupplier
    {
        @Override
        @Nullable
        public SpriteContents get() {
            try {
                NativeImage nativeImage = this.baseImage.get().mappedCopy(this.palette.get());
                SpriteContents spriteContents = new SpriteContents(this.permutationLocation, new FrameSize(nativeImage.getWidth(), nativeImage.getHeight()), nativeImage, AnimationMetadataSection.EMPTY);
                return spriteContents;
            } catch (IOException | IllegalArgumentException exception) {
                LOGGER.error("unable to apply palette to {}", (Object)this.permutationLocation, (Object)exception);
                SpriteContents spriteContents = null;
                return spriteContents;
            } finally {
                this.baseImage.release();
            }
        }

        @Override
        public void discard() {
            this.baseImage.release();
        }

        @Override
        @Nullable
        public /* synthetic */ Object get() {
            return this.get();
        }
    }
}

