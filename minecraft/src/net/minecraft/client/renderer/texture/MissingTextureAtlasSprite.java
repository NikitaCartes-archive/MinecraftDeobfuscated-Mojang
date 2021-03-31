package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;

@Environment(EnvType.CLIENT)
public final class MissingTextureAtlasSprite extends TextureAtlasSprite {
	private static final int MISSING_IMAGE_WIDTH = 16;
	private static final int MISSING_IMAGE_HEIGHT = 16;
	private static final String MISSING_TEXTURE_NAME = "missingno";
	private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
	@Nullable
	private static DynamicTexture missingTexture;
	private static final LazyLoadedValue<NativeImage> MISSING_IMAGE_DATA = new LazyLoadedValue<>(() -> {
		NativeImage nativeImage = new NativeImage(16, 16, false);
		int i = -16777216;
		int j = -524040;

		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				if (k < 8 ^ l < 8) {
					nativeImage.setPixelRGBA(l, k, -524040);
				} else {
					nativeImage.setPixelRGBA(l, k, -16777216);
				}
			}
		}

		nativeImage.untrack();
		return nativeImage;
	});
	private static final TextureAtlasSprite.Info INFO = new TextureAtlasSprite.Info(
		MISSING_TEXTURE_LOCATION, 16, 16, new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false)
	);

	private MissingTextureAtlasSprite(TextureAtlas textureAtlas, int i, int j, int k, int l, int m) {
		super(textureAtlas, INFO, i, j, k, l, m, MISSING_IMAGE_DATA.get());
	}

	public static MissingTextureAtlasSprite newInstance(TextureAtlas textureAtlas, int i, int j, int k, int l, int m) {
		return new MissingTextureAtlasSprite(textureAtlas, i, j, k, l, m);
	}

	public static ResourceLocation getLocation() {
		return MISSING_TEXTURE_LOCATION;
	}

	public static TextureAtlasSprite.Info info() {
		return INFO;
	}

	@Override
	public void close() {
		for (int i = 1; i < this.mainImage.length; i++) {
			this.mainImage[i].close();
		}
	}

	public static DynamicTexture getTexture() {
		if (missingTexture == null) {
			missingTexture = new DynamicTexture(MISSING_IMAGE_DATA.get());
			Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
		}

		return missingTexture;
	}
}
