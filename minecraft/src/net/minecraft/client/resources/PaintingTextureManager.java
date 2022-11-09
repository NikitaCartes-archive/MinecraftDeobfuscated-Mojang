package net.minecraft.client.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

@Environment(EnvType.CLIENT)
public class PaintingTextureManager extends TextureAtlasHolder {
	private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation("back");

	public PaintingTextureManager(TextureManager textureManager) {
		super(textureManager, new ResourceLocation("textures/atlas/paintings.png"), "painting");
	}

	public TextureAtlasSprite get(PaintingVariant paintingVariant) {
		return this.getSprite(BuiltInRegistries.PAINTING_VARIANT.getKey(paintingVariant));
	}

	public TextureAtlasSprite getBackSprite() {
		return this.getSprite(BACK_SPRITE_LOCATION);
	}
}
