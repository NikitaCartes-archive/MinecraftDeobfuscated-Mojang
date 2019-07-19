package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LayeredTexture extends AbstractTexture {
	private static final Logger LOGGER = LogManager.getLogger();
	public final List<String> layerPaths;

	public LayeredTexture(String... strings) {
		this.layerPaths = Lists.<String>newArrayList(strings);
		if (this.layerPaths.isEmpty()) {
			throw new IllegalStateException("Layered texture with no layers.");
		}
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
		Iterator<String> iterator = this.layerPaths.iterator();
		String string = (String)iterator.next();

		try {
			Resource resource = resourceManager.getResource(new ResourceLocation(string));
			Throwable var5 = null;

			try (NativeImage nativeImage = NativeImage.read(resource.getInputStream())) {
				while (true) {
					if (!iterator.hasNext()) {
						TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
						nativeImage.upload(0, 0, 0, false);
						break;
					}

					String string2 = (String)iterator.next();
					if (string2 != null) {
						Resource resource2 = resourceManager.getResource(new ResourceLocation(string2));
						Throwable var10 = null;

						try (NativeImage nativeImage2 = NativeImage.read(resource2.getInputStream())) {
							for (int i = 0; i < nativeImage2.getHeight(); i++) {
								for (int j = 0; j < nativeImage2.getWidth(); j++) {
									nativeImage.blendPixel(j, i, nativeImage2.getPixelRGBA(j, i));
								}
							}
						} catch (Throwable var91) {
							var10 = var91;
							throw var91;
						} finally {
							if (resource2 != null) {
								if (var10 != null) {
									try {
										resource2.close();
									} catch (Throwable var87) {
										var10.addSuppressed(var87);
									}
								} else {
									resource2.close();
								}
							}
						}
					}
				}
			} catch (Throwable var95) {
				var5 = var95;
				throw var95;
			} finally {
				if (resource != null) {
					if (var5 != null) {
						try {
							resource.close();
						} catch (Throwable var85) {
							var5.addSuppressed(var85);
						}
					} else {
						resource.close();
					}
				}
			}
		} catch (IOException var97) {
			LOGGER.error("Couldn't load layered image", (Throwable)var97);
		}
	}
}
