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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
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

	public CompletableFuture<SpriteLoader.Preparations> stitch(Map<ResourceLocation, Resource> map, int i, Executor executor) {
		return this.loadSpriteContents(map, executor)
			.thenApplyAsync(
				list -> {
					int j = this.maxSupportedTextureSize;
					Stitcher<SpriteContents> stitcher = new Stitcher<>(j, j, i);
					int k = Integer.MAX_VALUE;
					int l = 1 << i;

					for (SpriteContents spriteContents : list) {
						k = Math.min(k, Math.min(spriteContents.width(), spriteContents.height()));
						int m = Math.min(Integer.lowestOneBit(spriteContents.width()), Integer.lowestOneBit(spriteContents.height()));
						if (m < l) {
							LOGGER.warn(
								"Texture {} with size {}x{} limits mip level from {} to {}",
								spriteContents.name(),
								spriteContents.width(),
								spriteContents.height(),
								Mth.log2(l),
								Mth.log2(m)
							);
							l = m;
						}

						stitcher.registerSprite(spriteContents);
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

					try {
						stitcher.stitch();
					} catch (StitcherException var14) {
						CrashReport crashReport = CrashReport.forThrowable(var14, "Stitching");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
						crashReportCategory.setDetail(
							"Sprites",
							var14.getAllSprites()
								.stream()
								.map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height()))
								.collect(Collectors.joining(","))
						);
						crashReportCategory.setDetail("Max Texture Size", j);
						throw new ReportedException(crashReport);
					}

					Map<ResourceLocation, TextureAtlasSprite> mapx = this.getStitchedSprites(stitcher);
					TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)mapx.get(MissingTextureAtlasSprite.getLocation());
					CompletableFuture<Void> completableFuture;
					if (m > 0) {
						completableFuture = CompletableFuture.runAsync(
							() -> mapx.values().forEach(textureAtlasSpritex -> textureAtlasSpritex.contents().increaseMipLevel(m)), executor
						);
					} else {
						completableFuture = CompletableFuture.completedFuture(null);
					}

					return new SpriteLoader.Preparations(stitcher.getWidth(), stitcher.getHeight(), m, textureAtlasSprite, mapx, completableFuture);
				},
				executor
			);
	}

	private CompletableFuture<List<SpriteContents>> loadSpriteContents(Map<ResourceLocation, Resource> map, Executor executor) {
		List<CompletableFuture<SpriteContents>> list = new ArrayList();
		list.add(CompletableFuture.supplyAsync(MissingTextureAtlasSprite::create, executor));
		map.forEach((resourceLocation, resource) -> list.add(CompletableFuture.supplyAsync(() -> this.loadSprite(resourceLocation, resource), executor)));
		return Util.sequence(list).thenApply(listx -> listx.stream().filter(Objects::nonNull).toList());
	}

	@Nullable
	private SpriteContents loadSprite(ResourceLocation resourceLocation, Resource resource) {
		AnimationMetadataSection animationMetadataSection;
		try {
			animationMetadataSection = (AnimationMetadataSection)resource.metadata()
				.getSection(AnimationMetadataSection.SERIALIZER)
				.orElse(AnimationMetadataSection.EMPTY);
		} catch (Exception var9) {
			LOGGER.error("Unable to parse metadata from {} : {}", this.location, var9);
			return null;
		}

		NativeImage nativeImage;
		try {
			InputStream inputStream = resource.open();

			try {
				nativeImage = NativeImage.read(inputStream);
			} catch (Throwable var10) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var8) {
						var10.addSuppressed(var8);
					}
				}

				throw var10;
			}

			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException var11) {
			LOGGER.error("Using missing texture, unable to load {} : {}", this.location, var11);
			return null;
		}

		FrameSize frameSize = animationMetadataSection.calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
		if (Mth.isDivisionInteger(nativeImage.getWidth(), frameSize.width()) && Mth.isDivisionInteger(nativeImage.getHeight(), frameSize.height())) {
			return new SpriteContents(resourceLocation, frameSize, nativeImage, animationMetadataSection);
		} else {
			LOGGER.error(
				"Image {} size {},{} is not multiple of frame size {},{}",
				this.location,
				nativeImage.getWidth(),
				nativeImage.getHeight(),
				frameSize.width(),
				frameSize.height()
			);
			nativeImage.close();
			return null;
		}
	}

	private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher) {
		Map<ResourceLocation, TextureAtlasSprite> map = new HashMap();
		int i = stitcher.getWidth();
		int j = stitcher.getHeight();
		stitcher.gatherSprites((spriteContents, k, l) -> map.put(spriteContents.name(), new TextureAtlasSprite(this.location, spriteContents, i, j, k, l)));
		return map;
	}

	public static void addSprite(ResourceManager resourceManager, ResourceLocation resourceLocation, BiConsumer<ResourceLocation, Resource> biConsumer) {
		ResourceLocation resourceLocation2 = TEXTURE_ID_CONVERTER.idToFile(resourceLocation);
		Optional<Resource> optional = resourceManager.getResource(resourceLocation2);
		if (optional.isPresent()) {
			biConsumer.accept(resourceLocation, (Resource)optional.get());
		} else {
			LOGGER.warn("Missing sprite: {}", resourceLocation2);
		}
	}

	public static void listSprites(ResourceManager resourceManager, String string, BiConsumer<ResourceLocation, Resource> biConsumer) {
		listSprites(resourceManager, "textures/" + string, string + "/", biConsumer);
	}

	public static void listSprites(ResourceManager resourceManager, String string, String string2, BiConsumer<ResourceLocation, Resource> biConsumer) {
		FileToIdConverter fileToIdConverter = new FileToIdConverter(string, ".png");
		fileToIdConverter.listMatchingResources(resourceManager).forEach((resourceLocation, resource) -> {
			ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation).withPrefix(string2);
			biConsumer.accept(resourceLocation2, resource);
		});
	}

	public static Map<ResourceLocation, Resource> listSprites(ResourceManager resourceManager, String string) {
		return listSprites(resourceManager, "textures/" + string, string + "/");
	}

	public static Map<ResourceLocation, Resource> listSprites(ResourceManager resourceManager, String string, String string2) {
		Map<ResourceLocation, Resource> map = new HashMap();
		listSprites(resourceManager, string, string2, map::put);
		return map;
	}

	@Environment(EnvType.CLIENT)
	public static record Preparations(
		int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload
	) {
		public CompletableFuture<SpriteLoader.Preparations> waitForUpload() {
			return this.readyForUpload.thenApply(void_ -> this);
		}
	}
}
