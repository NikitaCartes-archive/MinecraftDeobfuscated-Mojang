package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.AbstractTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class DynamicTexture extends AbstractTexture implements AutoCloseable {
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
		RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
		this.pixels = new NativeImage(i, j, bl);
		TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
	}

	public void upload() {
		this.bind();
		this.pixels.upload(0, 0, 0, false);
	}

	@Nullable
	public NativeImage getPixels() {
		return this.pixels;
	}

	public void setPixels(NativeImage nativeImage) throws Exception {
		this.pixels.close();
		this.pixels = nativeImage;
	}

	public void close() {
		this.pixels.close();
		this.releaseId();
		this.pixels = null;
	}
}
