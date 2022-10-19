/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
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
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
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
    private static final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

    public SpriteLoader(ResourceLocation resourceLocation, int i) {
        this.location = resourceLocation;
        this.maxSupportedTextureSize = i;
    }

    public static SpriteLoader create(TextureAtlas textureAtlas) {
        return new SpriteLoader(textureAtlas.location(), textureAtlas.maxSupportedTextureSize());
    }

    public CompletableFuture<Preparations> stitch(Map<ResourceLocation, Resource> map, int i, Executor executor) {
        return this.loadSpriteContents(map, executor).thenApplyAsync(list -> {
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
        }, executor);
    }

    private CompletableFuture<List<SpriteContents>> loadSpriteContents(Map<ResourceLocation, Resource> map, Executor executor) {
        ArrayList<CompletableFuture<SpriteContents>> list2 = new ArrayList<CompletableFuture<SpriteContents>>();
        list2.add(CompletableFuture.supplyAsync(MissingTextureAtlasSprite::create, executor));
        map.forEach((resourceLocation, resource) -> list2.add(CompletableFuture.supplyAsync(() -> this.loadSprite((ResourceLocation)resourceLocation, (Resource)resource), executor)));
        return Util.sequence(list2).thenApply(list -> list.stream().filter(Objects::nonNull).toList());
    }

    @Nullable
    private SpriteContents loadSprite(ResourceLocation resourceLocation, Resource resource) {
        NativeImage nativeImage;
        AnimationMetadataSection animationMetadataSection;
        try {
            animationMetadataSection = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        } catch (Exception exception) {
            LOGGER.error("Unable to parse metadata from {} : {}", (Object)this.location, (Object)exception);
            return null;
        }
        try (InputStream inputStream = resource.open();){
            nativeImage = NativeImage.read(inputStream);
        } catch (IOException iOException) {
            LOGGER.error("Using missing texture, unable to load {} : {}", (Object)this.location, (Object)iOException);
            return null;
        }
        FrameSize frameSize = animationMetadataSection.calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
        if (!Mth.isDivisionInteger(nativeImage.getWidth(), frameSize.width()) || !Mth.isDivisionInteger(nativeImage.getHeight(), frameSize.height())) {
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", this.location, nativeImage.getWidth(), nativeImage.getHeight(), frameSize.width(), frameSize.height());
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

    public static void addSprite(ResourceManager resourceManager, ResourceLocation resourceLocation, BiConsumer<ResourceLocation, Resource> biConsumer) {
        ResourceLocation resourceLocation2 = TEXTURE_ID_CONVERTER.idToFile(resourceLocation);
        Optional<Resource> optional = resourceManager.getResource(resourceLocation2);
        if (optional.isPresent()) {
            biConsumer.accept(resourceLocation, optional.get());
        } else {
            LOGGER.warn("Missing sprite: {}", (Object)resourceLocation2);
        }
    }

    public static void listSprites(ResourceManager resourceManager, String string, BiConsumer<ResourceLocation, Resource> biConsumer) {
        SpriteLoader.listSprites(resourceManager, "textures/" + string, string + "/", biConsumer);
    }

    public static void listSprites(ResourceManager resourceManager, String string, String string2, BiConsumer<ResourceLocation, Resource> biConsumer) {
        FileToIdConverter fileToIdConverter = new FileToIdConverter(string, ".png");
        fileToIdConverter.listMatchingResources(resourceManager).forEach((resourceLocation, resource) -> {
            ResourceLocation resourceLocation2 = fileToIdConverter.fileToId((ResourceLocation)resourceLocation).withPrefix(string2);
            biConsumer.accept(resourceLocation2, (Resource)resource);
        });
    }

    public static Map<ResourceLocation, Resource> listSprites(ResourceManager resourceManager, String string) {
        return SpriteLoader.listSprites(resourceManager, "textures/" + string, string + "/");
    }

    public static Map<ResourceLocation, Resource> listSprites(ResourceManager resourceManager, String string, String string2) {
        HashMap<ResourceLocation, Resource> map = new HashMap<ResourceLocation, Resource>();
        SpriteLoader.listSprites(resourceManager, string, string2, map::put);
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
        public CompletableFuture<Preparations> waitForUpload() {
            return this.readyForUpload.thenApply(void_ -> this);
        }
    }
}

