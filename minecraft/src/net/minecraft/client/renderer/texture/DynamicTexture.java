package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class DynamicTexture extends AbstractTexture implements Dumpable {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	private NativeImage pixels;

	public DynamicTexture(NativeImage nativeImage) {
		this.pixels = nativeImage;
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
				this.upload();
			});
		} else {
			TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
			this.upload();
		}
	}

	public DynamicTexture(int i, int j, boolean bl) {
		this.pixels = new NativeImage(i, j, bl);
		TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
	}

	@Override
	public void load(ResourceManager resourceManager) {
	}

	public void upload() {
		if (this.pixels != null) {
			this.bind();
			this.pixels.upload(0, 0, 0, false);
		} else {
			LOGGER.warn("Trying to upload disposed texture {}", this.getId());
		}
	}

	@Nullable
	public NativeImage getPixels() {
		return this.pixels;
	}

	public void setPixels(NativeImage nativeImage) {
		if (this.pixels != null) {
			this.pixels.close();
		}

		this.pixels = nativeImage;
	}

	@Override
	public void close() {
		if (this.pixels != null) {
			this.pixels.close();
			this.releaseId();
			this.pixels = null;
		}
	}

	@Override
	public void dumpContents(ResourceLocation resourceLocation, Path path) throws IOException {
		if (this.pixels != null) {
			String string = resourceLocation.toDebugFileName() + ".png";
			Path path2 = path.resolve(string);
			this.pixels.writeToFile(path2);
		}
	}
}
