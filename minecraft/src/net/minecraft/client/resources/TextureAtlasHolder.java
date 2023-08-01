package net.minecraft.client.resources;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public abstract class TextureAtlasHolder implements PreparableReloadListener, AutoCloseable {
	private final TextureAtlas textureAtlas;
	private final ResourceLocation atlasInfoLocation;
	private final Set<MetadataSectionSerializer<?>> metadataSections;

	public TextureAtlasHolder(TextureManager textureManager, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		this(textureManager, resourceLocation, resourceLocation2, SpriteLoader.DEFAULT_METADATA_SECTIONS);
	}

	public TextureAtlasHolder(
		TextureManager textureManager, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, Set<MetadataSectionSerializer<?>> set
	) {
		this.atlasInfoLocation = resourceLocation2;
		this.textureAtlas = new TextureAtlas(resourceLocation);
		textureManager.register(this.textureAtlas.location(), this.textureAtlas);
		this.metadataSections = set;
	}

	protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		return this.textureAtlas.getSprite(resourceLocation);
	}

	@Override
	public final CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		return SpriteLoader.create(this.textureAtlas)
			.loadAndStitch(resourceManager, this.atlasInfoLocation, 0, executor, this.metadataSections)
			.thenCompose(SpriteLoader.Preparations::waitForUpload)
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(preparations -> this.apply(preparations, profilerFiller2), executor2);
	}

	private void apply(SpriteLoader.Preparations preparations, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		profilerFiller.push("upload");
		this.textureAtlas.upload(preparations);
		profilerFiller.pop();
		profilerFiller.endTick();
	}

	public void close() {
		this.textureAtlas.clearTextureData();
	}
}
