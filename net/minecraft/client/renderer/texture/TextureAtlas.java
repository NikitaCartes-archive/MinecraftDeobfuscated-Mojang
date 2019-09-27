/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
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
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureAtlas
extends AbstractTexture
implements Tickable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation LOCATION_BLOCKS = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    public static final ResourceLocation LOCATION_PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");
    public static final ResourceLocation LOCATION_MOB_EFFECTS = new ResourceLocation("textures/atlas/mob_effects.png");
    private final List<TextureAtlasSprite> animatedTextures = Lists.newArrayList();
    private final Set<ResourceLocation> sprites = Sets.newHashSet();
    private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.newHashMap();
    private final String path;
    private final int maxSupportedTextureSize;
    private int maxMipLevel;
    private final TextureAtlasSprite missingTextureSprite = MissingTextureAtlasSprite.newInstance();

    public TextureAtlas(String string) {
        this.path = string;
        this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
    }

    public void reload(Preparations preparations) {
        this.sprites.clear();
        this.sprites.addAll(preparations.sprites);
        LOGGER.info("Created: {}x{} {}-atlas", (Object)preparations.width, (Object)preparations.height, (Object)this.path);
        TextureUtil.prepareImage(this.getId(), this.maxMipLevel, preparations.width, preparations.height);
        this.clearTextureData();
        for (TextureAtlasSprite textureAtlasSprite : preparations.regions) {
            this.texturesByName.put(textureAtlasSprite.getName(), textureAtlasSprite);
            try {
                textureAtlasSprite.uploadFirstFrame();
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                crashReportCategory.setDetail("Atlas path", this.path);
                crashReportCategory.setDetail("Sprite", textureAtlasSprite);
                throw new ReportedException(crashReport);
            }
            if (!textureAtlasSprite.isAnimation()) continue;
            this.animatedTextures.add(textureAtlasSprite);
        }
    }

    public Preparations prepareToStitch(ResourceManager resourceManager, Iterable<ResourceLocation> iterable, ProfilerFiller profilerFiller) {
        HashSet<ResourceLocation> set = Sets.newHashSet();
        profilerFiller.push("preparing");
        iterable.forEach(resourceLocation -> {
            if (resourceLocation == null) {
                throw new IllegalArgumentException("Location cannot be null!");
            }
            set.add((ResourceLocation)resourceLocation);
        });
        int i = this.maxSupportedTextureSize;
        Stitcher stitcher = new Stitcher(i, i, this.maxMipLevel);
        int j = Integer.MAX_VALUE;
        int k = 1 << this.maxMipLevel;
        profilerFiller.popPush("extracting_frames");
        for (TextureAtlasSprite textureAtlasSprite2 : this.getBasicSpriteInfos(resourceManager, set)) {
            j = Math.min(j, Math.min(textureAtlasSprite2.getWidth(), textureAtlasSprite2.getHeight()));
            int l = Math.min(Integer.lowestOneBit(textureAtlasSprite2.getWidth()), Integer.lowestOneBit(textureAtlasSprite2.getHeight()));
            if (l < k) {
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", (Object)textureAtlasSprite2.getName(), (Object)textureAtlasSprite2.getWidth(), (Object)textureAtlasSprite2.getHeight(), (Object)Mth.log2(k), (Object)Mth.log2(l));
                k = l;
            }
            stitcher.registerSprite(textureAtlasSprite2);
        }
        int m = Math.min(j, k);
        int n = Mth.log2(m);
        if (n < this.maxMipLevel) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", (Object)this.path, (Object)this.maxMipLevel, (Object)n, (Object)m);
            this.maxMipLevel = n;
        }
        profilerFiller.popPush("mipmapping");
        this.missingTextureSprite.applyMipmapping(this.maxMipLevel);
        profilerFiller.popPush("register");
        stitcher.registerSprite(this.missingTextureSprite);
        profilerFiller.popPush("stitching");
        try {
            stitcher.stitch();
        } catch (StitcherException stitcherException) {
            CrashReport crashReport = CrashReport.forThrowable(stitcherException, "Stitching");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
            crashReportCategory.setDetail("Sprites", stitcherException.getAllSprites().stream().map(textureAtlasSprite -> String.format("%s[%dx%d]", textureAtlasSprite.getName(), textureAtlasSprite.getWidth(), textureAtlasSprite.getHeight())).collect(Collectors.joining(",")));
            crashReportCategory.setDetail("Max Texture Size", i);
            throw new ReportedException(crashReport);
        }
        profilerFiller.popPush("loading");
        List<TextureAtlasSprite> list = this.getLoadedSprites(resourceManager, stitcher);
        profilerFiller.pop();
        return new Preparations(set, stitcher.getWidth(), stitcher.getHeight(), list);
    }

    private Collection<TextureAtlasSprite> getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set) {
        ArrayList<CompletableFuture<Void>> list = Lists.newArrayList();
        ConcurrentLinkedQueue<TextureAtlasSprite> concurrentLinkedQueue = new ConcurrentLinkedQueue<TextureAtlasSprite>();
        for (ResourceLocation resourceLocation : set) {
            if (this.missingTextureSprite.getName().equals(resourceLocation)) continue;
            list.add(CompletableFuture.runAsync(() -> {
                TextureAtlasSprite textureAtlasSprite;
                ResourceLocation resourceLocation2 = this.getResourceLocation(resourceLocation);
                try (Resource resource = resourceManager.getResource(resourceLocation2);){
                    PngInfo pngInfo = new PngInfo(resource.toString(), resource.getInputStream());
                    AnimationMetadataSection animationMetadataSection = resource.getMetadata(AnimationMetadataSection.SERIALIZER);
                    textureAtlasSprite = new TextureAtlasSprite(resourceLocation, pngInfo, animationMetadataSection);
                } catch (RuntimeException runtimeException) {
                    LOGGER.error("Unable to parse metadata from {} : {}", (Object)resourceLocation2, (Object)runtimeException);
                    return;
                } catch (IOException iOException) {
                    LOGGER.error("Using missing texture, unable to load {} : {}", (Object)resourceLocation2, (Object)iOException);
                    return;
                }
                concurrentLinkedQueue.add(textureAtlasSprite);
            }, Util.backgroundExecutor()));
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return concurrentLinkedQueue;
    }

    private List<TextureAtlasSprite> getLoadedSprites(ResourceManager resourceManager, Stitcher stitcher) {
        ConcurrentLinkedQueue<TextureAtlasSprite> concurrentLinkedQueue = new ConcurrentLinkedQueue<TextureAtlasSprite>();
        ArrayList<CompletableFuture<Void>> list = Lists.newArrayList();
        for (TextureAtlasSprite textureAtlasSprite : stitcher.gatherSprites()) {
            if (textureAtlasSprite == this.missingTextureSprite) {
                concurrentLinkedQueue.add(textureAtlasSprite);
                continue;
            }
            list.add(CompletableFuture.runAsync(() -> {
                if (this.load(resourceManager, textureAtlasSprite)) {
                    concurrentLinkedQueue.add(textureAtlasSprite);
                }
            }, Util.backgroundExecutor()));
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(concurrentLinkedQueue);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean load(ResourceManager resourceManager, TextureAtlasSprite textureAtlasSprite) {
        ResourceLocation resourceLocation = this.getResourceLocation(textureAtlasSprite.getName());
        Resource resource = null;
        try {
            resource = resourceManager.getResource(resourceLocation);
            textureAtlasSprite.loadData(resource, this.maxMipLevel + 1);
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unable to parse metadata from {}", (Object)resourceLocation, (Object)runtimeException);
            boolean bl = false;
            return bl;
        } catch (IOException iOException) {
            LOGGER.error("Using missing texture, unable to load {}", (Object)resourceLocation, (Object)iOException);
            boolean bl = false;
            return bl;
        } finally {
            IOUtils.closeQuietly((Closeable)resource);
        }
        try {
            textureAtlasSprite.applyMipmapping(this.maxMipLevel);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Applying mipmap");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
            crashReportCategory.setDetail("Sprite name", () -> textureAtlasSprite.getName().toString());
            crashReportCategory.setDetail("Sprite size", () -> textureAtlasSprite.getWidth() + " x " + textureAtlasSprite.getHeight());
            crashReportCategory.setDetail("Sprite frames", () -> textureAtlasSprite.getFrameCount() + " frames");
            crashReportCategory.setDetail("Mipmap levels", this.maxMipLevel);
            throw new ReportedException(crashReport);
        }
        return true;
    }

    private ResourceLocation getResourceLocation(ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), String.format("%s/%s%s", this.path, resourceLocation.getPath(), ".png"));
    }

    public TextureAtlasSprite getTexture(String string) {
        return this.getSprite(new ResourceLocation(string));
    }

    public void cycleAnimationFrames() {
        this.bind();
        for (TextureAtlasSprite textureAtlasSprite : this.animatedTextures) {
            textureAtlasSprite.cycleFrames();
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

    public void setMaxMipLevel(int i) {
        this.maxMipLevel = i;
    }

    public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        TextureAtlasSprite textureAtlasSprite = this.texturesByName.get(resourceLocation);
        if (textureAtlasSprite == null) {
            return this.missingTextureSprite;
        }
        return textureAtlasSprite;
    }

    public void clearTextureData() {
        for (TextureAtlasSprite textureAtlasSprite : this.texturesByName.values()) {
            textureAtlasSprite.wipeFrameData();
        }
        this.texturesByName.clear();
        this.animatedTextures.clear();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Preparations {
        final Set<ResourceLocation> sprites;
        final int width;
        final int height;
        final List<TextureAtlasSprite> regions;

        public Preparations(Set<ResourceLocation> set, int i, int j, List<TextureAtlasSprite> list) {
            this.sprites = set;
            this.width = i;
            this.height = j;
            this.regions = list;
        }
    }
}

