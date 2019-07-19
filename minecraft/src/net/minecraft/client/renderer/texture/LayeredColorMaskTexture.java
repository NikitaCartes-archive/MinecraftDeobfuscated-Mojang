package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LayeredColorMaskTexture extends AbstractTexture {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ResourceLocation baseLayerResource;
	private final List<String> layerMaskPaths;
	private final List<DyeColor> layerColors;

	public LayeredColorMaskTexture(ResourceLocation resourceLocation, List<String> list, List<DyeColor> list2) {
		this.baseLayerResource = resourceLocation;
		this.layerMaskPaths = list;
		this.layerColors = list2;
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
		try {
			Resource resource = resourceManager.getResource(this.baseLayerResource);
			Throwable var3 = null;

			try (
				NativeImage nativeImage = NativeImage.read(resource.getInputStream());
				NativeImage nativeImage2 = new NativeImage(nativeImage.getWidth(), nativeImage.getHeight(), false);
			) {
				nativeImage2.copyFrom(nativeImage);

				for (int i = 0; i < 17 && i < this.layerMaskPaths.size() && i < this.layerColors.size(); i++) {
					String string = (String)this.layerMaskPaths.get(i);
					if (string != null) {
						Resource resource2 = resourceManager.getResource(new ResourceLocation(string));
						Throwable var11 = null;

						try (NativeImage nativeImage3 = NativeImage.read(resource2.getInputStream())) {
							int j = ((DyeColor)this.layerColors.get(i)).getTextureDiffuseColorBGR();
							if (nativeImage3.getWidth() == nativeImage2.getWidth() && nativeImage3.getHeight() == nativeImage2.getHeight()) {
								for (int k = 0; k < nativeImage3.getHeight(); k++) {
									for (int l = 0; l < nativeImage3.getWidth(); l++) {
										int m = nativeImage3.getPixelRGBA(l, k);
										if ((m & 0xFF000000) != 0) {
											int n = (m & 0xFF) << 24 & 0xFF000000;
											int o = nativeImage.getPixelRGBA(l, k);
											int p = Mth.colorMultiply(o, j) & 16777215;
											nativeImage2.blendPixel(l, k, n | p);
										}
									}
								}
							}
						} catch (Throwable var142) {
							var11 = var142;
							throw var142;
						} finally {
							if (resource2 != null) {
								if (var11 != null) {
									try {
										resource2.close();
									} catch (Throwable var138) {
										var11.addSuppressed(var138);
									}
								} else {
									resource2.close();
								}
							}
						}
					}
				}

				TextureUtil.prepareImage(this.getId(), nativeImage2.getWidth(), nativeImage2.getHeight());
				GlStateManager.pixelTransfer(3357, Float.MAX_VALUE);
				nativeImage2.upload(0, 0, 0, false);
				GlStateManager.pixelTransfer(3357, 0.0F);
			} catch (Throwable var148) {
				var3 = var148;
				throw var148;
			} finally {
				if (resource != null) {
					if (var3 != null) {
						try {
							resource.close();
						} catch (Throwable var135) {
							var3.addSuppressed(var135);
						}
					} else {
						resource.close();
					}
				}
			}
		} catch (IOException var150) {
			LOGGER.error("Couldn't load layered color mask image", (Throwable)var150);
		}
	}
}
