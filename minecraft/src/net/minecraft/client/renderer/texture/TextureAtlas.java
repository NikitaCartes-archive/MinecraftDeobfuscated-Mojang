package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class TextureAtlas extends AbstractTexture implements Tickable {
	private static final Logger LOGGER = LogManager.getLogger();
	@Deprecated
	public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
	@Deprecated
	public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
	private final List<TextureAtlasSprite> animatedTextures = Lists.<TextureAtlasSprite>newArrayList();
	private final Set<ResourceLocation> sprites = Sets.<ResourceLocation>newHashSet();
	private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.<ResourceLocation, TextureAtlasSprite>newHashMap();
	private final ResourceLocation location;
	private final int maxSupportedTextureSize;

	public TextureAtlas(ResourceLocation resourceLocation) {
		this.location = resourceLocation;
		this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
	}

	@Override
	public void load(ResourceManager resourceManager) {
	}

	public void reload(TextureAtlas.Preparations preparations) {
		this.sprites.clear();
		this.sprites.addAll(preparations.sprites);
		LOGGER.info("Created: {}x{}x{} {}-atlas", preparations.width, preparations.height, preparations.mipLevel, this.location);
		TextureUtil.prepareImage(this.getId(), preparations.mipLevel, preparations.width, preparations.height);
		this.clearTextureData();

		for (TextureAtlasSprite textureAtlasSprite : preparations.regions) {
			this.texturesByName.put(textureAtlasSprite.getName(), textureAtlasSprite);

			try {
				textureAtlasSprite.uploadFirstFrame();
			} catch (Throwable var7) {
				CrashReport crashReport = CrashReport.forThrowable(var7, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", this.location);
				crashReportCategory.setDetail("Sprite", textureAtlasSprite);
				throw new ReportedException(crashReport);
			}

			if (textureAtlasSprite.isAnimation()) {
				this.animatedTextures.add(textureAtlasSprite);
			}
		}
	}

	public TextureAtlas.Preparations prepareToStitch(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i) {
		profilerFiller.push("preparing");
		Set<ResourceLocation> set = (Set<ResourceLocation>)stream.peek(resourceLocation -> {
			if (resourceLocation == null) {
				throw new IllegalArgumentException("Location cannot be null!");
			}
		}).collect(Collectors.toSet());
		int j = this.maxSupportedTextureSize;
		Stitcher stitcher = new Stitcher(j, j, i);
		int k = Integer.MAX_VALUE;
		int l = 1 << i;
		profilerFiller.popPush("extracting_frames");

		for (TextureAtlasSprite.Info info : this.getBasicSpriteInfos(resourceManager, set)) {
			k = Math.min(k, Math.min(info.width(), info.height()));
			int m = Math.min(Integer.lowestOneBit(info.width()), Integer.lowestOneBit(info.height()));
			if (m < l) {
				LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", info.name(), info.width(), info.height(), Mth.log2(l), Mth.log2(m));
				l = m;
			}

			stitcher.registerSprite(info);
		}

		int n = Math.min(k, l);
		int o = Mth.log2(n);
		int m;
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
		} catch (StitcherException var16) {
			CrashReport crashReport = CrashReport.forThrowable(var16, "Stitching");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
			crashReportCategory.setDetail(
				"Sprites",
				var16.getAllSprites().stream().map(info -> String.format("%s[%dx%d]", info.name(), info.width(), info.height())).collect(Collectors.joining(","))
			);
			crashReportCategory.setDetail("Max Texture Size", j);
			throw new ReportedException(crashReport);
		}

		profilerFiller.popPush("loading");
		List<TextureAtlasSprite> list = this.getLoadedSprites(resourceManager, stitcher, m);
		profilerFiller.pop();
		return new TextureAtlas.Preparations(set, stitcher.getWidth(), stitcher.getHeight(), m, list);
	}

	private Collection<TextureAtlasSprite.Info> getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set) {
		List<CompletableFuture<?>> list = Lists.<CompletableFuture<?>>newArrayList();
		Queue<TextureAtlasSprite.Info> queue = new ConcurrentLinkedQueue();

		for (ResourceLocation resourceLocation : set) {
			if (!MissingTextureAtlasSprite.getLocation().equals(resourceLocation)) {
				list.add(CompletableFuture.runAsync(() -> {
					ResourceLocation resourceLocation2 = this.getResourceLocation(resourceLocation);

					TextureAtlasSprite.Info info;
					try {
						Resource resource = resourceManager.getResource(resourceLocation2);
						Throwable var7 = null;

						try {
							PngInfo pngInfo = new PngInfo(resource.toString(), resource.getInputStream());
							AnimationMetadataSection animationMetadataSection = resource.getMetadata(AnimationMetadataSection.SERIALIZER);
							if (animationMetadataSection == null) {
								animationMetadataSection = AnimationMetadataSection.EMPTY;
							}

							Pair<Integer, Integer> pair = animationMetadataSection.getFrameSize(pngInfo.width, pngInfo.height);
							info = new TextureAtlasSprite.Info(resourceLocation, pair.getFirst(), pair.getSecond(), animationMetadataSection);
						} catch (Throwable var20) {
							var7 = var20;
							throw var20;
						} finally {
							if (resource != null) {
								if (var7 != null) {
									try {
										resource.close();
									} catch (Throwable var19) {
										var7.addSuppressed(var19);
									}
								} else {
									resource.close();
								}
							}
						}
					} catch (RuntimeException var22) {
						LOGGER.error("Unable to parse metadata from {} : {}", resourceLocation2, var22);
						return;
					} catch (IOException var23) {
						LOGGER.error("Using missing texture, unable to load {} : {}", resourceLocation2, var23);
						return;
					}

					queue.add(info);
				}, Util.backgroundExecutor()));
			}
		}

		CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0])).join();
		return queue;
	}

	private List<TextureAtlasSprite> getLoadedSprites(ResourceManager resourceManager, Stitcher stitcher, int i) {
		Queue<TextureAtlasSprite> queue = new ConcurrentLinkedQueue();
		List<CompletableFuture<?>> list = Lists.<CompletableFuture<?>>newArrayList();
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
		CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0])).join();
		return Lists.<TextureAtlasSprite>newArrayList(queue);
	}

	@Nullable
	private TextureAtlasSprite load(ResourceManager resourceManager, TextureAtlasSprite.Info info, int i, int j, int k, int l, int m) {
		ResourceLocation resourceLocation = this.getResourceLocation(info.name());

		try {
			Resource resource = resourceManager.getResource(resourceLocation);
			Throwable var10 = null;

			TextureAtlasSprite var12;
			try {
				NativeImage nativeImage = NativeImage.read(resource.getInputStream());
				var12 = new TextureAtlasSprite(this, info, k, i, j, l, m, nativeImage);
			} catch (Throwable var23) {
				var10 = var23;
				throw var23;
			} finally {
				if (resource != null) {
					if (var10 != null) {
						try {
							resource.close();
						} catch (Throwable var22) {
							var10.addSuppressed(var22);
						}
					} else {
						resource.close();
					}
				}
			}

			return var12;
		} catch (RuntimeException var25) {
			LOGGER.error("Unable to parse metadata from {}", resourceLocation, var25);
			return null;
		} catch (IOException var26) {
			LOGGER.error("Using missing texture, unable to load {}", resourceLocation, var26);
			return null;
		}
	}

	private ResourceLocation getResourceLocation(ResourceLocation resourceLocation) {
		return new ResourceLocation(resourceLocation.getNamespace(), String.format("textures/%s%s", resourceLocation.getPath(), ".png"));
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

	public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)this.texturesByName.get(resourceLocation);
		return textureAtlasSprite == null ? (TextureAtlasSprite)this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : textureAtlasSprite;
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

	public void updateFilter(TextureAtlas.Preparations preparations) {
		this.setFilter(false, preparations.mipLevel > 0);
	}

	@Environment(EnvType.CLIENT)
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
