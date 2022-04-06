package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public interface SpriteSet {
	TextureAtlasSprite get(int i, int j);

	TextureAtlasSprite get(RandomSource randomSource);
}
