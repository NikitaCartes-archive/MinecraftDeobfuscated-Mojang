package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public interface SpriteSet {
	TextureAtlasSprite get(int i, int j);

	TextureAtlasSprite get(Random random);
}
