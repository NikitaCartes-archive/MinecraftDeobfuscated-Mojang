package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class LegacyStuffWrapper {
	@Deprecated
	public static int[] getPixels(ResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException {
		Resource resource = resourceManager.getResource(resourceLocation);

		int[] var4;
		try (NativeImage nativeImage = NativeImage.read(resource.getInputStream())) {
			var4 = nativeImage.makePixelArray();
		} catch (Throwable var9) {
			if (resource != null) {
				try {
					resource.close();
				} catch (Throwable var6) {
					var9.addSuppressed(var6);
				}
			}

			throw var9;
		}

		if (resource != null) {
			resource.close();
		}

		return var4;
	}
}
