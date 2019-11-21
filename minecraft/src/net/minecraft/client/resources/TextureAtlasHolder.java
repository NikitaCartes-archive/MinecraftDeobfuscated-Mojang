package net.minecraft.client.resources;

import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public abstract class TextureAtlasHolder extends SimplePreparableReloadListener<TextureAtlas.Preparations> implements AutoCloseable {
	private final TextureAtlas textureAtlas;
	private final String prefix;

	public TextureAtlasHolder(TextureManager textureManager, ResourceLocation resourceLocation, String string) {
		this.prefix = string;
		this.textureAtlas = new TextureAtlas(resourceLocation);
		textureManager.register(this.textureAtlas.location(), this.textureAtlas);
	}

	protected abstract Stream<ResourceLocation> getResourcesToLoad();

	protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		return this.textureAtlas.getSprite(this.resolveLocation(resourceLocation));
	}

	private ResourceLocation resolveLocation(ResourceLocation resourceLocation) {
		return new ResourceLocation(resourceLocation.getNamespace(), this.prefix + "/" + resourceLocation.getPath());
	}

	protected TextureAtlas.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		profilerFiller.push("stitching");
		TextureAtlas.Preparations preparations = this.textureAtlas
			.prepareToStitch(resourceManager, this.getResourcesToLoad().map(this::resolveLocation), profilerFiller, 0);
		profilerFiller.pop();
		profilerFiller.endTick();
		return preparations;
	}

	protected void apply(TextureAtlas.Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		profilerFiller.push("upload");
		this.textureAtlas.reload(preparations);
		profilerFiller.pop();
		profilerFiller.endTick();
	}

	public void close() {
		this.textureAtlas.clearTextureData();
	}
}
