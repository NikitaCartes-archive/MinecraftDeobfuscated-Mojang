package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsTextureManager {
	private static final Map<String, RealmsTextureManager.RealmsTexture> TEXTURES = Maps.<String, RealmsTextureManager.RealmsTexture>newHashMap();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

	public static ResourceLocation worldTemplate(String string, @Nullable String string2) {
		return string2 == null ? TEMPLATE_ICON_LOCATION : getTexture(string, string2);
	}

	private static ResourceLocation getTexture(String string, String string2) {
		RealmsTextureManager.RealmsTexture realmsTexture = (RealmsTextureManager.RealmsTexture)TEXTURES.get(string);
		if (realmsTexture != null && realmsTexture.image().equals(string2)) {
			return realmsTexture.textureId;
		} else {
			NativeImage nativeImage = loadImage(string2);
			if (nativeImage == null) {
				ResourceLocation resourceLocation = MissingTextureAtlasSprite.getLocation();
				TEXTURES.put(string, new RealmsTextureManager.RealmsTexture(string2, resourceLocation));
				return resourceLocation;
			} else {
				ResourceLocation resourceLocation = new ResourceLocation("realms", "dynamic/" + string);
				Minecraft.getInstance().getTextureManager().register(resourceLocation, new DynamicTexture(nativeImage));
				TEXTURES.put(string, new RealmsTextureManager.RealmsTexture(string2, resourceLocation));
				return resourceLocation;
			}
		}
	}

	@Nullable
	private static NativeImage loadImage(String string) {
		byte[] bs = Base64.getDecoder().decode(string);
		ByteBuffer byteBuffer = MemoryUtil.memAlloc(bs.length);

		try {
			return NativeImage.read(byteBuffer.put(bs).flip());
		} catch (IOException var7) {
			LOGGER.warn("Failed to load world image: {}", string, var7);
		} finally {
			MemoryUtil.memFree(byteBuffer);
		}

		return null;
	}

	@Environment(EnvType.CLIENT)
	public static record RealmsTexture(String image, ResourceLocation textureId) {
	}
}
