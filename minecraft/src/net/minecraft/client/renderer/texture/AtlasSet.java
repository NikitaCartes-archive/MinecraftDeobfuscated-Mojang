package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AtlasSet implements AutoCloseable {
	private final Map<ResourceLocation, TextureAtlas> atlases;

	public AtlasSet(Collection<TextureAtlas> collection) {
		this.atlases = (Map<ResourceLocation, TextureAtlas>)collection.stream().collect(Collectors.toMap(TextureAtlas::location, Function.identity()));
	}

	public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
		return (TextureAtlas)this.atlases.get(resourceLocation);
	}

	public TextureAtlasSprite getSprite(Material material) {
		return ((TextureAtlas)this.atlases.get(material.atlasLocation())).getSprite(material.texture());
	}

	public void close() {
		this.atlases.values().forEach(TextureAtlas::clearTextureData);
		this.atlases.clear();
	}
}
