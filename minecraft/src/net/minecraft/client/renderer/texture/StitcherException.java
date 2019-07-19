package net.minecraft.client.renderer.texture;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StitcherException extends RuntimeException {
	private final Collection<TextureAtlasSprite> allSprites;

	public StitcherException(TextureAtlasSprite textureAtlasSprite, Collection<TextureAtlasSprite> collection) {
		super(
			String.format(
				"Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?",
				textureAtlasSprite.getName(),
				textureAtlasSprite.getWidth(),
				textureAtlasSprite.getHeight()
			)
		);
		this.allSprites = collection;
	}

	public Collection<TextureAtlasSprite> getAllSprites() {
		return this.allSprites;
	}
}
