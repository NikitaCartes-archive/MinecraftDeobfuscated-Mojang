package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class TextureAtlas extends AbstractTexture implements TickableTextureObject {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ResourceLocation LOCATION_BLOCKS = new ResourceLocation("textures/atlas/blocks.png");
	public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
	public static final ResourceLocation LOCATION_PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");
	public static final ResourceLocation LOCATION_MOB_EFFECTS = new ResourceLocation("textures/atlas/mob_effects.png");
	private final List<TextureAtlasSprite> animatedTextures = Lists.<TextureAtlasSprite>newArrayList();
	private final Set<ResourceLocation> sprites = Sets.<ResourceLocation>newHashSet();
	private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.<ResourceLocation, TextureAtlasSprite>newHashMap();
	private final String path;
	private final int maxSupportedTextureSize;
	private int maxMipLevel;
	private final TextureAtlasSprite missingTextureSprite = MissingTextureAtlasSprite.newInstance();

	public TextureAtlas(String string) {
		this.path = string;
		this.maxSupportedTextureSize = Minecraft.maxSupportedTextureSize();
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
	}

	public void reload(TextureAtlas.Preparations preparations) {
		this.sprites.clear();
		this.sprites.addAll(preparations.sprites);
		LOGGER.info("Created: {}x{} {}-atlas", preparations.width, preparations.height, this.path);
		TextureUtil.prepareImage(this.getId(), this.maxMipLevel, preparations.width, preparations.height);
		this.clearTextureData();

		for (TextureAtlasSprite textureAtlasSprite : preparations.regions) {
			this.texturesByName.put(textureAtlasSprite.getName(), textureAtlasSprite);

			try {
				textureAtlasSprite.uploadFirstFrame();
			} catch (Throwable var7) {
				CrashReport crashReport = CrashReport.forThrowable(var7, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", this.path);
				crashReportCategory.setDetail("Sprite", textureAtlasSprite);
				throw new ReportedException(crashReport);
			}

			if (textureAtlasSprite.isAnimation()) {
				this.animatedTextures.add(textureAtlasSprite);
			}
		}
	}

	public TextureAtlas.Preparations prepareToStitch(ResourceManager resourceManager, Iterable<ResourceLocation> iterable, ProfilerFiller profilerFiller) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		profilerFiller.push("preparing");
		iterable.forEach(resourceLocation -> {
			if (resourceLocation == null) {
				throw new IllegalArgumentException("Location cannot be null!");
			} else {
				set.add(resourceLocation);
			}
		});
		int i = this.maxSupportedTextureSize;
		Stitcher stitcher = new Stitcher(i, i, this.maxMipLevel);
		int j = Integer.MAX_VALUE;
		int k = 1 << this.maxMipLevel;
		profilerFiller.popPush("extracting_frames");

		for (TextureAtlasSprite textureAtlasSprite : this.getBasicSpriteInfos(resourceManager, set)) {
			j = Math.min(j, Math.min(textureAtlasSprite.getWidth(), textureAtlasSprite.getHeight()));
			int l = Math.min(Integer.lowestOneBit(textureAtlasSprite.getWidth()), Integer.lowestOneBit(textureAtlasSprite.getHeight()));
			if (l < k) {
				LOGGER.warn(
					"Texture {} with size {}x{} limits mip level from {} to {}",
					textureAtlasSprite.getName(),
					textureAtlasSprite.getWidth(),
					textureAtlasSprite.getHeight(),
					Mth.log2(k),
					Mth.log2(l)
				);
				k = l;
			}

			stitcher.registerSprite(textureAtlasSprite);
		}

		int m = Math.min(j, k);
		int n = Mth.log2(m);
		if (n < this.maxMipLevel) {
			LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.path, this.maxMipLevel, n, m);
			this.maxMipLevel = n;
		}

		profilerFiller.popPush("mipmapping");
		this.missingTextureSprite.applyMipmapping(this.maxMipLevel);
		profilerFiller.popPush("register");
		stitcher.registerSprite(this.missingTextureSprite);
		profilerFiller.popPush("stitching");

		try {
			stitcher.stitch();
		} catch (StitcherException var14) {
			CrashReport crashReport = CrashReport.forThrowable(var14, "Stitching");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
			crashReportCategory.setDetail(
				"Sprites",
				var14.getAllSprites()
					.stream()
					.map(textureAtlasSprite -> String.format("%s[%dx%d]", textureAtlasSprite.getName(), textureAtlasSprite.getWidth(), textureAtlasSprite.getHeight()))
					.collect(Collectors.joining(","))
			);
			crashReportCategory.setDetail("Max Texture Size", i);
			throw new ReportedException(crashReport);
		}

		profilerFiller.popPush("loading");
		List<TextureAtlasSprite> list = this.getLoadedSprites(resourceManager, stitcher);
		profilerFiller.pop();
		return new TextureAtlas.Preparations(set, stitcher.getWidth(), stitcher.getHeight(), list);
	}

	private Collection<TextureAtlasSprite> getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set) {
		List<CompletableFuture<?>> list = new ArrayList();
		ConcurrentLinkedQueue<TextureAtlasSprite> concurrentLinkedQueue = new ConcurrentLinkedQueue();

		for (ResourceLocation resourceLocation : set) {
			if (!this.missingTextureSprite.getName().equals(resourceLocation)) {
				list.add(CompletableFuture.runAsync(() -> {
					ResourceLocation resourceLocation2 = this.getResourceLocation(resourceLocation);

					TextureAtlasSprite textureAtlasSprite;
					try {
						Resource resource = resourceManager.getResource(resourceLocation2);
						Throwable var7 = null;

						try {
							PngInfo pngInfo = new PngInfo(resource.toString(), resource.getInputStream());
							AnimationMetadataSection animationMetadataSection = resource.getMetadata(AnimationMetadataSection.SERIALIZER);
							textureAtlasSprite = new TextureAtlasSprite(resourceLocation, pngInfo, animationMetadataSection);
						} catch (Throwable var19) {
							var7 = var19;
							throw var19;
						} finally {
							if (resource != null) {
								if (var7 != null) {
									try {
										resource.close();
									} catch (Throwable var18) {
										var7.addSuppressed(var18);
									}
								} else {
									resource.close();
								}
							}
						}
					} catch (RuntimeException var21) {
						LOGGER.error("Unable to parse metadata from {} : {}", resourceLocation2, var21);
						return;
					} catch (IOException var22) {
						LOGGER.error("Using missing texture, unable to load {} : {}", resourceLocation2, var22);
						return;
					}

					concurrentLinkedQueue.add(textureAtlasSprite);
				}, Util.backgroundExecutor()));
			}
		}

		CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0])).join();
		return concurrentLinkedQueue;
	}

	private List<TextureAtlasSprite> getLoadedSprites(ResourceManager resourceManager, Stitcher stitcher) {
		ConcurrentLinkedQueue<TextureAtlasSprite> concurrentLinkedQueue = new ConcurrentLinkedQueue();
		List<CompletableFuture<?>> list = new ArrayList();

		for (TextureAtlasSprite textureAtlasSprite : stitcher.gatherSprites()) {
			if (textureAtlasSprite == this.missingTextureSprite) {
				concurrentLinkedQueue.add(textureAtlasSprite);
			} else {
				list.add(CompletableFuture.runAsync(() -> {
					if (this.load(resourceManager, textureAtlasSprite)) {
						concurrentLinkedQueue.add(textureAtlasSprite);
					}
				}, Util.backgroundExecutor()));
			}
		}

		CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0])).join();
		return new ArrayList(concurrentLinkedQueue);
	}

	private boolean load(ResourceManager resourceManager, TextureAtlasSprite textureAtlasSprite) {
		ResourceLocation resourceLocation = this.getResourceLocation(textureAtlasSprite.getName());
		Resource resource = null;

		label45: {
			boolean crashReport;
			try {
				resource = resourceManager.getResource(resourceLocation);
				textureAtlasSprite.loadData(resource, this.maxMipLevel + 1);
				break label45;
			} catch (RuntimeException var13) {
				LOGGER.error("Unable to parse metadata from {}", resourceLocation, var13);
				return false;
			} catch (IOException var14) {
				LOGGER.error("Using missing texture, unable to load {}", resourceLocation, var14);
				crashReport = false;
			} finally {
				IOUtils.closeQuietly(resource);
			}

			return crashReport;
		}

		try {
			textureAtlasSprite.applyMipmapping(this.maxMipLevel);
			return true;
		} catch (Throwable var12) {
			CrashReport crashReport = CrashReport.forThrowable(var12, "Applying mipmap");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
			crashReportCategory.setDetail("Sprite name", (CrashReportDetail<String>)(() -> textureAtlasSprite.getName().toString()));
			crashReportCategory.setDetail("Sprite size", (CrashReportDetail<String>)(() -> textureAtlasSprite.getWidth() + " x " + textureAtlasSprite.getHeight()));
			crashReportCategory.setDetail("Sprite frames", (CrashReportDetail<String>)(() -> textureAtlasSprite.getFrameCount() + " frames"));
			crashReportCategory.setDetail("Mipmap levels", this.maxMipLevel);
			throw new ReportedException(crashReport);
		}
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
		this.cycleAnimationFrames();
	}

	public void setMaxMipLevel(int i) {
		this.maxMipLevel = i;
	}

	public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)this.texturesByName.get(resourceLocation);
		return textureAtlasSprite == null ? this.missingTextureSprite : textureAtlasSprite;
	}

	public void clearTextureData() {
		for (TextureAtlasSprite textureAtlasSprite : this.texturesByName.values()) {
			textureAtlasSprite.wipeFrameData();
		}

		this.texturesByName.clear();
		this.animatedTextures.clear();
	}

	@Environment(EnvType.CLIENT)
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
