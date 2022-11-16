package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
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

@Environment(EnvType.CLIENT)
public class Unstitcher implements SpriteSource {
	static final Logger LOGGER = LogUtils.getLogger();
	private final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");
	public static final Codec<Unstitcher> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("resource").forGetter(unstitcher -> unstitcher.resource),
					ExtraCodecs.nonEmptyList(Unstitcher.Region.CODEC.listOf()).fieldOf("regions").forGetter(unstitcher -> unstitcher.regions),
					Codec.DOUBLE.optionalFieldOf("divisor_x", Double.valueOf(1.0)).forGetter(unstitcher -> unstitcher.xDivisor),
					Codec.DOUBLE.optionalFieldOf("divisor_y", Double.valueOf(1.0)).forGetter(unstitcher -> unstitcher.yDivisor)
				)
				.apply(instance, Unstitcher::new)
	);
	private final ResourceLocation resource;
	private final List<Unstitcher.Region> regions;
	private final double xDivisor;
	private final double yDivisor;

	public Unstitcher(ResourceLocation resourceLocation, List<Unstitcher.Region> list, double d, double e) {
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
			Unstitcher.LazyLoadedImage lazyLoadedImage = new Unstitcher.LazyLoadedImage(resourceLocation, (Resource)optional.get(), this.regions.size());

			for (Unstitcher.Region region : this.regions) {
				output.add(region.sprite, new Unstitcher.RegionInstance(lazyLoadedImage, region, this.xDivisor, this.yDivisor));
			}
		} else {
			LOGGER.warn("Missing sprite: {}", resourceLocation);
		}
	}

	@Override
	public SpriteSourceType type() {
		return SpriteSources.UNSTITCHER;
	}

	@Environment(EnvType.CLIENT)
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

		public NativeImage get() throws IOException {
			NativeImage nativeImage = (NativeImage)this.image.get();
			if (nativeImage == null) {
				synchronized (this) {
					nativeImage = (NativeImage)this.image.get();
					if (nativeImage == null) {
						try {
							InputStream inputStream = this.resource.open();

							try {
								nativeImage = NativeImage.read(inputStream);
								this.image.set(nativeImage);
							} catch (Throwable var8) {
								if (inputStream != null) {
									try {
										inputStream.close();
									} catch (Throwable var7) {
										var8.addSuppressed(var7);
									}
								}

								throw var8;
							}

							if (inputStream != null) {
								inputStream.close();
							}
						} catch (IOException var9) {
							throw new IOException("Failed to load image " + this.id, var9);
						}
					}
				}
			}

			return nativeImage;
		}

		public void release() {
			int i = this.referenceCount.decrementAndGet();
			if (i <= 0) {
				NativeImage nativeImage = (NativeImage)this.image.getAndSet(null);
				if (nativeImage != null) {
					nativeImage.close();
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record Region(ResourceLocation sprite, double x, double y, double width, double height) {
		public static final Codec<Unstitcher.Region> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceLocation.CODEC.fieldOf("sprite").forGetter(Unstitcher.Region::sprite),
						Codec.DOUBLE.fieldOf("x").forGetter(Unstitcher.Region::x),
						Codec.DOUBLE.fieldOf("y").forGetter(Unstitcher.Region::y),
						Codec.DOUBLE.fieldOf("width").forGetter(Unstitcher.Region::width),
						Codec.DOUBLE.fieldOf("height").forGetter(Unstitcher.Region::height)
					)
					.apply(instance, Unstitcher.Region::new)
		);
	}

	@Environment(EnvType.CLIENT)
	static class RegionInstance implements SpriteSource.SpriteSupplier {
		private final Unstitcher.LazyLoadedImage image;
		private final Unstitcher.Region region;
		private final double xDivisor;
		private final double yDivisor;

		RegionInstance(Unstitcher.LazyLoadedImage lazyLoadedImage, Unstitcher.Region region, double d, double e) {
			this.image = lazyLoadedImage;
			this.region = region;
			this.xDivisor = d;
			this.yDivisor = e;
		}

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
				return new SpriteContents(this.region.sprite, new FrameSize(k, l), nativeImage2, AnimationMetadataSection.EMPTY);
			} catch (Exception var15) {
				Unstitcher.LOGGER.error("Failed to unstitch region {}", this.region.sprite, var15);
			} finally {
				this.image.release();
			}

			return MissingTextureAtlasSprite.create();
		}

		@Override
		public void discard() {
			this.image.release();
		}
	}
}
