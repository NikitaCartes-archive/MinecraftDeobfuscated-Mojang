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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TextureAtlas extends AbstractTexture implements Tickable {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Deprecated
	public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
	@Deprecated
	public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
	private static final String FILE_EXTENSION = ".png";
	private final List<Tickable> animatedTextures = Lists.<Tickable>newArrayList();
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

			Tickable tickable = textureAtlasSprite.getAnimationTicker();
			if (tickable != null) {
				this.animatedTextures.add(tickable);
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
				list.add(
					CompletableFuture.runAsync(
						() -> {
							ResourceLocation resourceLocation2 = this.getResourceLocation(resourceLocation);
							Optional<Resource> optional = resourceManager.getResource(resourceLocation2);
							if (optional.isEmpty()) {
								LOGGER.error("Using missing texture, file {} not found", resourceLocation2);
							} else {
								Resource resource = (Resource)optional.get();

								PngInfo pngInfo;
								try {
									InputStream inputStream = resource.open();

									try {
										pngInfo = new PngInfo(resourceLocation2::toString, inputStream);
									} catch (Throwable var14) {
										if (inputStream != null) {
											try {
												inputStream.close();
											} catch (Throwable var12) {
												var14.addSuppressed(var12);
											}
										}

										throw var14;
									}

									if (inputStream != null) {
										inputStream.close();
									}
								} catch (IOException var15) {
									LOGGER.error("Using missing texture, unable to load {} : {}", resourceLocation2, var15);
									return;
								}

								AnimationMetadataSection animationMetadataSection;
								try {
									animationMetadataSection = (AnimationMetadataSection)resource.metadata()
										.getSection(AnimationMetadataSection.SERIALIZER)
										.orElse(AnimationMetadataSection.EMPTY);
								} catch (Exception var13) {
									LOGGER.error("Unable to parse metadata from {} : {}", resourceLocation2, var13);
									return;
								}

								Pair<Integer, Integer> pair = animationMetadataSection.getFrameSize(pngInfo.width, pngInfo.height);
								TextureAtlasSprite.Info info = new TextureAtlasSprite.Info(resourceLocation, pair.getFirst(), pair.getSecond(), animationMetadataSection);
								queue.add(info);
							}
						},
						Util.backgroundExecutor()
					)
				);
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
			InputStream inputStream = resourceManager.open(resourceLocation);

			TextureAtlasSprite var11;
			try {
				NativeImage nativeImage = NativeImage.read(inputStream);
				var11 = new TextureAtlasSprite(this, info, k, i, j, l, m, nativeImage);
			} catch (Throwable var13) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var12) {
						var13.addSuppressed(var12);
					}
				}

				throw var13;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var11;
		} catch (RuntimeException var14) {
			LOGGER.error("Unable to parse metadata from {}", resourceLocation, var14);
			return null;
		} catch (IOException var15) {
			LOGGER.error("Using missing texture, unable to load {}", resourceLocation, var15);
			return null;
		}
	}

	private ResourceLocation getResourceLocation(ResourceLocation resourceLocation) {
		return new ResourceLocation(resourceLocation.getNamespace(), String.format("textures/%s%s", resourceLocation.getPath(), ".png"));
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
