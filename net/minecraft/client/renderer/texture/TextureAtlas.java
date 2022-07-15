/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureAtlas
extends AbstractTexture
implements Tickable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private static final String FILE_EXTENSION = ".png";
    private final List<Tickable> animatedTextures = Lists.newArrayList();
    private final Set<ResourceLocation> sprites = Sets.newHashSet();
    private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.newHashMap();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;

    public TextureAtlas(ResourceLocation resourceLocation) {
        this.location = resourceLocation;
        this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    @Override
    public void load(ResourceManager resourceManager) {
    }

    public void reload(Preparations preparations) {
        this.sprites.clear();
        this.sprites.addAll(preparations.sprites);
        LOGGER.info("Created: {}x{}x{} {}-atlas", preparations.width, preparations.height, preparations.mipLevel, this.location);
        TextureUtil.prepareImage(this.getId(), preparations.mipLevel, preparations.width, preparations.height);
        this.clearTextureData();
        for (TextureAtlasSprite textureAtlasSprite : preparations.regions) {
            this.texturesByName.put(textureAtlasSprite.getName(), textureAtlasSprite);
            try {
                textureAtlasSprite.uploadFirstFrame();
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                crashReportCategory.setDetail("Atlas path", this.location);
                crashReportCategory.setDetail("Sprite", textureAtlasSprite);
                throw new ReportedException(crashReport);
            }
            Tickable tickable = textureAtlasSprite.getAnimationTicker();
            if (tickable == null) continue;
            this.animatedTextures.add(tickable);
        }
    }

    public Preparations prepareToStitch(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i) {
        int m;
        profilerFiller.push("preparing");
        Set<ResourceLocation> set = stream.peek(resourceLocation -> {
            if (resourceLocation == null) {
                throw new IllegalArgumentException("Location cannot be null!");
            }
        }).collect(Collectors.toSet());
        int j = this.maxSupportedTextureSize;
        Stitcher stitcher = new Stitcher(j, j, i);
        int k = Integer.MAX_VALUE;
        int l = 1 << i;
        profilerFiller.popPush("extracting_frames");
        for (TextureAtlasSprite.Info info2 : this.getBasicSpriteInfos(resourceManager, set)) {
            k = Math.min(k, Math.min(info2.width(), info2.height()));
            m = Math.min(Integer.lowestOneBit(info2.width()), Integer.lowestOneBit(info2.height()));
            if (m < l) {
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", info2.name(), info2.width(), info2.height(), Mth.log2(l), Mth.log2(m));
                l = m;
            }
            stitcher.registerSprite(info2);
        }
        int n = Math.min(k, l);
        int o = Mth.log2(n);
        if (o < i) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, i, o, n);
            m = o;
        } else {
            m = i;
        }
        profilerFiller.popPush("register");
        stitcher.registerSprite(MissingTextureAtlasSprite.info());
        profilerFiller.popPush("stitching");
        try {
            stitcher.stitch();
        } catch (StitcherException stitcherException) {
            CrashReport crashReport = CrashReport.forThrowable(stitcherException, "Stitching");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
            crashReportCategory.setDetail("Sprites", stitcherException.getAllSprites().stream().map(info -> String.format(Locale.ROOT, "%s[%dx%d]", info.name(), info.width(), info.height())).collect(Collectors.joining(",")));
            crashReportCategory.setDetail("Max Texture Size", j);
            throw new ReportedException(crashReport);
        }
        profilerFiller.popPush("loading");
        List<TextureAtlasSprite> list = this.getLoadedSprites(resourceManager, stitcher, m);
        profilerFiller.pop();
        return new Preparations(set, stitcher.getWidth(), stitcher.getHeight(), m, list);
    }

    private Collection<TextureAtlasSprite.Info> getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set) {
        ArrayList<CompletableFuture<Void>> list = Lists.newArrayList();
        ConcurrentLinkedQueue<TextureAtlasSprite.Info> queue = new ConcurrentLinkedQueue<TextureAtlasSprite.Info>();
        for (ResourceLocation resourceLocation : set) {
            if (MissingTextureAtlasSprite.getLocation().equals(resourceLocation)) continue;
            list.add(CompletableFuture.runAsync(() -> {
                AnimationMetadataSection animationMetadataSection;
                PngInfo pngInfo;
                ResourceLocation resourceLocation2 = this.getResourceLocation(resourceLocation);
                Optional<Resource> optional = resourceManager.getResource(resourceLocation2);
                if (optional.isEmpty()) {
                    LOGGER.error("Using missing texture, file {} not found", (Object)resourceLocation2);
                    return;
                }
                Resource resource = optional.get();
                try (InputStream inputStream = resource.open();){
                    pngInfo = new PngInfo(resourceLocation2::toString, inputStream);
                } catch (IOException iOException) {
                    LOGGER.error("Using missing texture, unable to load {} : {}", (Object)resourceLocation2, (Object)iOException);
                    return;
                }
                try {
                    animationMetadataSection = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
                } catch (Exception exception) {
                    LOGGER.error("Unable to parse metadata from {} : {}", (Object)resourceLocation2, (Object)exception);
                    return;
                }
                Pair<Integer, Integer> pair = animationMetadataSection.getFrameSize(pngInfo.width, pngInfo.height);
                TextureAtlasSprite.Info info = new TextureAtlasSprite.Info(resourceLocation, pair.getFirst(), pair.getSecond(), animationMetadataSection);
                queue.add(info);
            }, Util.backgroundExecutor()));
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return queue;
    }

    private List<TextureAtlasSprite> getLoadedSprites(ResourceManager resourceManager, Stitcher stitcher, int i) {
        ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
        ArrayList list = Lists.newArrayList();
        stitcher.gatherSprites((info, j, k, l, m) -> {
            if (info == MissingTextureAtlasSprite.info()) {
                MissingTextureAtlasSprite missingTextureAtlasSprite = MissingTextureAtlasSprite.newInstance(this, i, j, k, l, m);
                queue.add(missingTextureAtlasSprite);
            } else {
                list.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite textureAtlasSprite = this.load(resourceManager, info, j, k, i, l, m);
                    if (textureAtlasSprite != null) {
                        queue.add(textureAtlasSprite);
                    }
                }, Util.backgroundExecutor()));
            }
        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(queue);
    }

    @Nullable
    private TextureAtlasSprite load(ResourceManager resourceManager, TextureAtlasSprite.Info info, int i, int j, int k, int l, int m) {
        TextureAtlasSprite textureAtlasSprite;
        block9: {
            ResourceLocation resourceLocation = this.getResourceLocation(info.name());
            InputStream inputStream = resourceManager.open(resourceLocation);
            try {
                NativeImage nativeImage = NativeImage.read(inputStream);
                textureAtlasSprite = new TextureAtlasSprite(this, info, k, i, j, l, m, nativeImage);
                if (inputStream == null) break block9;
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
                } catch (RuntimeException runtimeException) {
                    LOGGER.error("Unable to parse metadata from {}", (Object)resourceLocation, (Object)runtimeException);
                    return null;
                } catch (IOException iOException) {
                    LOGGER.error("Using missing texture, unable to load {}", (Object)resourceLocation, (Object)iOException);
                    return null;
                }
            }
            inputStream.close();
        }
        return textureAtlasSprite;
    }

    private ResourceLocation getResourceLocation(ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), String.format(Locale.ROOT, "textures/%s%s", resourceLocation.getPath(), FILE_EXTENSION));
    }

    public void cycleAnimationFrames() {
        this.bind();
        for (Tickable tickable : this.animatedTextures) {
            tickable.tick();
        }
    }

    @Override
    public void tick() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::cycleAnimationFrames);
        } else {
            this.cycleAnimationFrames();
        }
    }

    public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        TextureAtlasSprite textureAtlasSprite = this.texturesByName.get(resourceLocation);
        if (textureAtlasSprite == null) {
            return this.texturesByName.get(MissingTextureAtlasSprite.getLocation());
        }
        return textureAtlasSprite;
    }

    public void clearTextureData() {
        for (TextureAtlasSprite textureAtlasSprite : this.texturesByName.values()) {
            textureAtlasSprite.close();
        }
        this.texturesByName.clear();
        this.animatedTextures.clear();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public void updateFilter(Preparations preparations) {
        this.setFilter(false, preparations.mipLevel > 0);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Preparations {
        final Set<ResourceLocation> sprites;
        final int width;
        final int height;
        final int mipLevel;
        final List<TextureAtlasSprite> regions;

        public Preparations(Set<ResourceLocation> set, int i, int j, int k, List<TextureAtlasSprite> list) {
            this.sprites = set;
            this.width = i;
            this.height = j;
            this.mipLevel = k;
            this.regions = list;
        }
    }
}

