package net.minecraft.client.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public abstract class TextureAtlasHolder implements PreparableReloadListener, AutoCloseable {
	private final TextureAtlas textureAtlas;
	private final String prefix;

	public TextureAtlasHolder(TextureManager textureManager, ResourceLocation resourceLocation, String string) {
		this.prefix = string;
		this.textureAtlas = new TextureAtlas(resourceLocation);
		textureManager.register(this.textureAtlas.location(), this.textureAtlas);
	}

	protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		return this.textureAtlas.getSprite(this.resolveLocation(resourceLocation));
	}

	private ResourceLocation resolveLocation(ResourceLocation resourceLocation) {
		return resourceLocation.withPrefix(this.prefix + "/");
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
		return CompletableFuture.supplyAsync(() -> SpriteLoader.listSprites(resourceManager, this.prefix), executor)
			.thenCompose(map -> SpriteLoader.create(this.textureAtlas).stitch(map, 0, executor))
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
