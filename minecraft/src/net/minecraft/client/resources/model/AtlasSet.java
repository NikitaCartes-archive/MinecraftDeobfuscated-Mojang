package net.minecraft.client.resources.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class AtlasSet implements AutoCloseable {
	private final Map<ResourceLocation, AtlasSet.AtlasEntry> atlases;

	public AtlasSet(Map<ResourceLocation, AtlasSet.ResourceLister> map, TextureManager textureManager) {
		this.atlases = (Map<ResourceLocation, AtlasSet.AtlasEntry>)map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
			TextureAtlas textureAtlas = new TextureAtlas((ResourceLocation)entry.getKey());
			textureManager.register((ResourceLocation)entry.getKey(), textureAtlas);
			return new AtlasSet.AtlasEntry(textureAtlas, (AtlasSet.ResourceLister)entry.getValue());
		}));
	}

	public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
		return ((AtlasSet.AtlasEntry)this.atlases.get(resourceLocation)).atlas();
	}

	public void close() {
		this.atlases.values().forEach(AtlasSet.AtlasEntry::close);
		this.atlases.clear();
	}

	public Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> scheduleLoad(ResourceManager resourceManager, int i, Executor executor) {
		return (Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>>)this.atlases
			.entrySet()
			.stream()
			.collect(
				Collectors.toMap(
					Entry::getKey,
					entry -> {
						AtlasSet.AtlasEntry atlasEntry = (AtlasSet.AtlasEntry)entry.getValue();
						return CompletableFuture.supplyAsync(() -> (Map)atlasEntry.resourceLister.apply(resourceManager), executor)
							.thenCompose(map -> SpriteLoader.create(atlasEntry.atlas).stitch(map, i, executor))
							.thenApply(preparations -> new AtlasSet.StitchResult(atlasEntry.atlas, preparations));
					}
				)
			);
	}

	@Environment(EnvType.CLIENT)
	static record AtlasEntry(TextureAtlas atlas, AtlasSet.ResourceLister resourceLister) implements AutoCloseable {

		public void close() {
			this.atlas.clearTextureData();
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface ResourceLister extends Function<ResourceManager, Map<ResourceLocation, Resource>> {
	}

	@Environment(EnvType.CLIENT)
	public static class StitchResult {
		private final TextureAtlas atlas;
		private final SpriteLoader.Preparations preparations;

		public StitchResult(TextureAtlas textureAtlas, SpriteLoader.Preparations preparations) {
			this.atlas = textureAtlas;
			this.preparations = preparations;
		}

		@Nullable
		public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
			return (TextureAtlasSprite)this.preparations.regions().get(resourceLocation);
		}

		public TextureAtlasSprite missing() {
			return this.preparations.missing();
		}

		public CompletableFuture<Void> readyForUpload() {
			return this.preparations.readyForUpload();
		}

		public void upload() {
			this.atlas.upload(this.preparations);
		}
	}
}
