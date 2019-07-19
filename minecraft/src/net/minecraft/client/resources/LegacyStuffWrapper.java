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
		Throwable var3 = null;

		int[] var6;
		try (NativeImage nativeImage = NativeImage.read(resource.getInputStream())) {
			var6 = nativeImage.makePixelArray();
		} catch (Throwable var31) {
			var3 = var31;
			throw var31;
		} finally {
			if (resource != null) {
				if (var3 != null) {
					try {
						resource.close();
					} catch (Throwable var27) {
						var3.addSuppressed(var27);
					}
				} else {
					resource.close();
				}
			}
		}

		return var6;
	}
}
