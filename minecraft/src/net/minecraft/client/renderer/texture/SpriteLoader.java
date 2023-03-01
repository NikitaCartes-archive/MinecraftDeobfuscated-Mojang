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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
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
	private final int minWidth;
	private final int minHeight;

	public SpriteLoader(ResourceLocation resourceLocation, int i, int j, int k) {
		this.location = resourceLocation;
		this.maxSupportedTextureSize = i;
		this.minWidth = j;
		this.minHeight = k;
	}

	public static SpriteLoader create(TextureAtlas textureAtlas) {
		return new SpriteLoader(textureAtlas.location(), textureAtlas.maxSupportedTextureSize(), textureAtlas.getWidth(), textureAtlas.getHeight());
	}

	public SpriteLoader.Preparations stitch(List<SpriteContents> list, int i, Executor executor) {
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
		} catch (StitcherException var16) {
			CrashReport crashReport = CrashReport.forThrowable(var16, "Stitching");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
			crashReportCategory.setDetail(
				"Sprites",
				var16.getAllSprites()
					.stream()
					.map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height()))
					.collect(Collectors.joining(","))
			);
			crashReportCategory.setDetail("Max Texture Size", j);
			throw new ReportedException(crashReport);
		}

		int p = Math.max(stitcher.getWidth(), this.minWidth);
		int q = Math.max(stitcher.getHeight(), this.minHeight);
		Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, p, q);
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)map.get(MissingTextureAtlasSprite.getLocation());
		CompletableFuture<Void> completableFuture;
		if (m > 0) {
			completableFuture = CompletableFuture.runAsync(
				() -> map.values().forEach(textureAtlasSpritex -> textureAtlasSpritex.contents().increaseMipLevel(m)), executor
			);
		} else {
			completableFuture = CompletableFuture.completedFuture(null);
		}

		return new SpriteLoader.Preparations(p, q, m, textureAtlasSprite, map, completableFuture);
	}

	public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(List<Supplier<SpriteContents>> list, Executor executor) {
		List<CompletableFuture<SpriteContents>> list2 = list.stream().map(supplier -> CompletableFuture.supplyAsync(supplier, executor)).toList();
		return Util.sequence(list2).thenApply(listx -> listx.stream().filter(Objects::nonNull).toList());
	}

	public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(ResourceManager resourceManager, ResourceLocation resourceLocation, int i, Executor executor) {
		return CompletableFuture.supplyAsync(() -> SpriteResourceLoader.load(resourceManager, resourceLocation).list(resourceManager), executor)
			.thenCompose(list -> runSpriteSuppliers(list, executor))
			.thenApply(list -> this.stitch(list, i, executor));
	}

	@Nullable
	public static SpriteContents loadSprite(ResourceLocation resourceLocation, Resource resource) {
		AnimationMetadataSection animationMetadataSection;
		try {
			animationMetadataSection = (AnimationMetadataSection)resource.metadata()
				.getSection(AnimationMetadataSection.SERIALIZER)
				.orElse(AnimationMetadataSection.EMPTY);
		} catch (Exception var8) {
			LOGGER.error("Unable to parse metadata from {}", resourceLocation, var8);
			return null;
		}

		NativeImage nativeImage;
		try {
			InputStream inputStream = resource.open();

			try {
				nativeImage = NativeImage.read(inputStream);
			} catch (Throwable var9) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var7) {
						var9.addSuppressed(var7);
					}
				}

				throw var9;
			}

			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException var10) {
			LOGGER.error("Using missing texture, unable to load {}", resourceLocation, var10);
			return null;
		}

		FrameSize frameSize = animationMetadataSection.calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
		if (Mth.isMultipleOf(nativeImage.getWidth(), frameSize.width()) && Mth.isMultipleOf(nativeImage.getHeight(), frameSize.height())) {
			return new SpriteContents(resourceLocation, frameSize, nativeImage, animationMetadataSection);
		} else {
			LOGGER.error(
				"Image {} size {},{} is not multiple of frame size {},{}",
				resourceLocation,
				nativeImage.getWidth(),
				nativeImage.getHeight(),
				frameSize.width(),
				frameSize.height()
			);
			nativeImage.close();
			return null;
		}
	}

	private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int i, int j) {
		Map<ResourceLocation, TextureAtlasSprite> map = new HashMap();
		stitcher.gatherSprites((spriteContents, k, l) -> map.put(spriteContents.name(), new TextureAtlasSprite(this.location, spriteContents, i, j, k, l)));
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
