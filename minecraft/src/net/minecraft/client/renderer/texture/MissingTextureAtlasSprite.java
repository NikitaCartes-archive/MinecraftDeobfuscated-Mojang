package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

@Environment(EnvType.CLIENT)
public final class MissingTextureAtlasSprite {
	private static final int MISSING_IMAGE_WIDTH = 16;
	private static final int MISSING_IMAGE_HEIGHT = 16;
	private static final String MISSING_TEXTURE_NAME = "missingno";
	private static final ResourceLocation MISSING_TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("missingno");
	private static final ResourceMetadata SPRITE_METADATA = new ResourceMetadata.Builder()
		.put(AnimationMetadataSection.SERIALIZER, new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false))
		.build();
	@Nullable
	private static DynamicTexture missingTexture;

	private static NativeImage generateMissingImage(int i, int j) {
		NativeImage nativeImage = new NativeImage(i, j, false);
		int k = -16777216;
		int l = -524040;

		for (int m = 0; m < j; m++) {
			for (int n = 0; n < i; n++) {
				if (m < j / 2 ^ n < i / 2) {
					nativeImage.setPixelRGBA(n, m, -524040);
				} else {
					nativeImage.setPixelRGBA(n, m, -16777216);
				}
			}
		}

		return nativeImage;
	}

	public static SpriteContents create() {
		NativeImage nativeImage = generateMissingImage(16, 16);
		return new SpriteContents(MISSING_TEXTURE_LOCATION, new FrameSize(16, 16), nativeImage, SPRITE_METADATA);
	}

	public static ResourceLocation getLocation() {
		return MISSING_TEXTURE_LOCATION;
	}

	public static DynamicTexture getTexture() {
		if (missingTexture == null) {
			NativeImage nativeImage = generateMissingImage(16, 16);
			nativeImage.untrack();
			missingTexture = new DynamicTexture(nativeImage);
			Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
		}

		return missingTexture;
	}
}
