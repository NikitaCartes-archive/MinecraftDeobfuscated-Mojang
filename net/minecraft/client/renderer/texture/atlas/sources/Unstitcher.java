/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Unstitcher
implements SpriteSource {
    static final Logger LOGGER = LogUtils.getLogger();
    private final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");
    public static final Codec<Unstitcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("resource")).forGetter(unstitcher -> unstitcher.resource), ((MapCodec)ExtraCodecs.nonEmptyList(Region.CODEC.listOf()).fieldOf("regions")).forGetter(unstitcher -> unstitcher.regions), Codec.DOUBLE.optionalFieldOf("divisor_x", 1.0).forGetter(unstitcher -> unstitcher.xDivisor), Codec.DOUBLE.optionalFieldOf("divisor_y", 1.0).forGetter(unstitcher -> unstitcher.yDivisor)).apply((Applicative<Unstitcher, ?>)instance, Unstitcher::new));
    private final ResourceLocation resource;
    private final List<Region> regions;
    private final double xDivisor;
    private final double yDivisor;

    public Unstitcher(ResourceLocation resourceLocation, List<Region> list, double d, double e) {
        this.resource = resourceLocation;
        this.regions = list;
        this.xDivisor = d;
        this.yDivisor = e;
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        ResourceLocation resourceLocation = this.TEXTURE_ID_CONVERTER.idToFile(this.resource);
        Optional<Resource> optional = resourceManager.getResource(resourceLocation);
        if (optional.isPresent()) {
            LazyLoadedImage lazyLoadedImage = new LazyLoadedImage(resourceLocation, optional.get(), this.regions.size());
            for (Region region : this.regions) {
                output.add(region.sprite, new RegionInstance(lazyLoadedImage, region, this.xDivisor, this.yDivisor));
            }
        } else {
            LOGGER.warn("Missing sprite: {}", (Object)resourceLocation);
        }
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.UNSTITCHER;
    }

    @Environment(value=EnvType.CLIENT)
    static class LazyLoadedImage {
        private final ResourceLocation id;
        private final Resource resource;
        private final AtomicReference<NativeImage> image = new AtomicReference();
        private final AtomicInteger referenceCount;

        LazyLoadedImage(ResourceLocation resourceLocation, Resource resource, int i) {
            this.id = resourceLocation;
            this.resource = resource;
            this.referenceCount = new AtomicInteger(i);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public NativeImage get() throws IOException {
            NativeImage nativeImage = this.image.get();
            if (nativeImage == null) {
                LazyLoadedImage lazyLoadedImage = this;
                synchronized (lazyLoadedImage) {
                    nativeImage = this.image.get();
                    if (nativeImage == null) {
                        try (InputStream inputStream = this.resource.open();){
                            nativeImage = NativeImage.read(inputStream);
                            this.image.set(nativeImage);
                        } catch (IOException iOException) {
                            throw new IOException("Failed to load image " + this.id, iOException);
                        }
                    }
                }
            }
            return nativeImage;
        }

        public void release() {
            NativeImage nativeImage;
            int i = this.referenceCount.decrementAndGet();
            if (i <= 0 && (nativeImage = (NativeImage)this.image.getAndSet(null)) != null) {
                nativeImage.close();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Region(ResourceLocation sprite, double x, double y, double width, double height) {
        public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("sprite")).forGetter(Region::sprite), ((MapCodec)Codec.DOUBLE.fieldOf("x")).forGetter(Region::x), ((MapCodec)Codec.DOUBLE.fieldOf("y")).forGetter(Region::y), ((MapCodec)Codec.DOUBLE.fieldOf("width")).forGetter(Region::width), ((MapCodec)Codec.DOUBLE.fieldOf("height")).forGetter(Region::height)).apply((Applicative<Region, ?>)instance, Region::new));
    }

    @Environment(value=EnvType.CLIENT)
    static class RegionInstance
    implements SpriteSource.SpriteSupplier {
        private final LazyLoadedImage image;
        private final Region region;
        private final double xDivisor;
        private final double yDivisor;

        RegionInstance(LazyLoadedImage lazyLoadedImage, Region region, double d, double e) {
            this.image = lazyLoadedImage;
            this.region = region;
            this.xDivisor = d;
            this.yDivisor = e;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public SpriteContents get() {
            try {
                NativeImage nativeImage = this.image.get();
                double d = (double)nativeImage.getWidth() / this.xDivisor;
                double e = (double)nativeImage.getHeight() / this.yDivisor;
                int i = Mth.floor(this.region.x * d);
                int j = Mth.floor(this.region.y * e);
                int k = Mth.floor(this.region.width * d);
                int l = Mth.floor(this.region.height * e);
                NativeImage nativeImage2 = new NativeImage(NativeImage.Format.RGBA, k, l, false);
                nativeImage.copyRect(nativeImage2, i, j, 0, 0, k, l, false, false);
                SpriteContents spriteContents = new SpriteContents(this.region.sprite, new FrameSize(k, l), nativeImage2, AnimationMetadataSection.EMPTY);
                return spriteContents;
            } catch (Exception exception) {
                LOGGER.error("Failed to unstitch region {}", (Object)this.region.sprite, (Object)exception);
            } finally {
                this.image.release();
            }
            return MissingTextureAtlasSprite.create();
        }

        @Override
        public void discard() {
            this.image.release();
        }

        @Override
        public /* synthetic */ Object get() {
            return this.get();
        }
    }
}

