/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;

    public SpriteLoader(ResourceLocation resourceLocation, int i) {
        this.location = resourceLocation;
        this.maxSupportedTextureSize = i;
    }

    public static SpriteLoader create(TextureAtlas textureAtlas) {
        return new SpriteLoader(textureAtlas.location(), textureAtlas.maxSupportedTextureSize());
    }

    public Preparations stitch(List<SpriteContents> list, int i, Executor executor) {
        int m;
        int j = this.maxSupportedTextureSize;
        Stitcher<SpriteContents> stitcher = new Stitcher<SpriteContents>(j, j, i);
        int k = Integer.MAX_VALUE;
        int l = 1 << i;
        for (SpriteContents spriteContents : list) {
            k = Math.min(k, Math.min(spriteContents.width(), spriteContents.height()));
            m = Math.min(Integer.lowestOneBit(spriteContents.width()), Integer.lowestOneBit(spriteContents.height()));
            if (m < l) {
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", spriteContents.name(), spriteContents.width(), spriteContents.height(), Mth.log2(l), Mth.log2(m));
                l = m;
            }
            stitcher.registerSprite(spriteContents);
        }
        int n = Math.min(k, l);
        int o = Mth.log2(n);
        if (o < i) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, i, o, n);
            m = o;
        } else {
            m = i;
        }
        try {
            stitcher.stitch();
        } catch (StitcherException stitcherException) {
            CrashReport crashReport = CrashReport.forThrowable(stitcherException, "Stitching");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
            crashReportCategory.setDetail("Sprites", stitcherException.getAllSprites().stream().map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height())).collect(Collectors.joining(",")));
            crashReportCategory.setDetail("Max Texture Size", j);
            throw new ReportedException(crashReport);
        }
        Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher);
        TextureAtlasSprite textureAtlasSprite = map.get(MissingTextureAtlasSprite.getLocation());
        CompletableFuture<Object> completableFuture = m > 0 ? CompletableFuture.runAsync(() -> map.values().forEach(textureAtlasSprite -> textureAtlasSprite.contents().increaseMipLevel(m)), executor) : CompletableFuture.completedFuture(null);
        return new Preparations(stitcher.getWidth(), stitcher.getHeight(), m, textureAtlasSprite, map, completableFuture);
    }

    public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(List<Supplier<SpriteContents>> list2, Executor executor) {
        List<CompletableFuture> list22 = list2.stream().map(supplier -> CompletableFuture.supplyAsync(supplier, executor)).toList();
        return Util.sequence(list22).thenApply(list -> list.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<Preparations> loadAndStitch(ResourceManager resourceManager, ResourceLocation resourceLocation, int i, Executor executor) {
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> SpriteResourceLoader.load(resourceManager, resourceLocation).list(resourceManager), executor).thenCompose(list -> SpriteLoader.runSpriteSuppliers(list, executor))).thenApply(list -> this.stitch((List<SpriteContents>)list, i, executor));
    }

    @Nullable
    public static SpriteContents loadSprite(ResourceLocation resourceLocation, Resource resource) {
        NativeImage nativeImage;
        AnimationMetadataSection animationMetadataSection;
        try {
            animationMetadataSection = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        } catch (Exception exception) {
            LOGGER.error("Unable to parse metadata from {}", (Object)resourceLocation, (Object)exception);
            return null;
        }
        try (InputStream inputStream = resource.open();){
            nativeImage = NativeImage.read(inputStream);
        } catch (IOException iOException) {
            LOGGER.error("Using missing texture, unable to load {}", (Object)resourceLocation, (Object)iOException);
            return null;
        }
        FrameSize frameSize = animationMetadataSection.calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
        if (!Mth.isDivisionInteger(nativeImage.getWidth(), frameSize.width()) || !Mth.isDivisionInteger(nativeImage.getHeight(), frameSize.height())) {
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", resourceLocation, nativeImage.getWidth(), nativeImage.getHeight(), frameSize.width(), frameSize.height());
            nativeImage.close();
            return null;
        }
        return new SpriteContents(resourceLocation, frameSize, nativeImage, animationMetadataSection);
    }

    private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher) {
        HashMap<ResourceLocation, TextureAtlasSprite> map = new HashMap<ResourceLocation, TextureAtlasSprite>();
        int i = stitcher.getWidth();
        int j = stitcher.getHeight();
        stitcher.gatherSprites((spriteContents, k, l) -> map.put(spriteContents.name(), new TextureAtlasSprite(this.location, (SpriteContents)spriteContents, i, j, k, l)));
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
        public CompletableFuture<Preparations> waitForUpload() {
            return this.readyForUpload.thenApply(void_ -> this);
        }
    }
}

