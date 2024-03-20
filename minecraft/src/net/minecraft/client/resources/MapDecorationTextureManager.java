package net.minecraft.client.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

@Environment(EnvType.CLIENT)
public class MapDecorationTextureManager extends TextureAtlasHolder {
	public MapDecorationTextureManager(TextureManager textureManager) {
		super(textureManager, new ResourceLocation("textures/atlas/map_decorations.png"), new ResourceLocation("map_decorations"));
	}

	public TextureAtlasSprite get(MapDecoration mapDecoration) {
		return this.getSprite(mapDecoration.getSpriteLocation());
	}
}
