package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StitcherException extends RuntimeException {
	private final Collection<TextureAtlasSprite.Info> allSprites;

	public StitcherException(TextureAtlasSprite.Info info, Collection<TextureAtlasSprite.Info> collection) {
		super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", info.name(), info.width(), info.height()));
		this.allSprites = collection;
	}

	public Collection<TextureAtlasSprite.Info> getAllSprites() {
		return this.allSprites;
	}
}
