package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class LegacyStuffWrapper {
	@Deprecated
	public static int[] getPixels(ResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException {
		InputStream inputStream = resourceManager.open(resourceLocation);

		int[] var4;
		try (NativeImage nativeImage = NativeImage.read(inputStream)) {
			var4 = nativeImage.makePixelArray();
		} catch (Throwable var9) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable var6) {
					var9.addSuppressed(var6);
				}
			}

			throw var9;
		}

		if (inputStream != null) {
			inputStream.close();
		}

		return var4;
	}
}
