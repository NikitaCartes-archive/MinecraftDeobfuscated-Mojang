package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class OverlayTexture implements AutoCloseable {
	public static final int NO_OVERLAY = pack(0, 10);
	private final DynamicTexture texture = new DynamicTexture(16, 16, false);

	public OverlayTexture() {
		NativeImage nativeImage = this.texture.getPixels();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				if (i < 8) {
					nativeImage.setPixelRGBA(j, i, -1308622593);
				} else {
					int k = (int)((1.0F - (float)j / 15.0F * 0.75F) * 255.0F);
					nativeImage.setPixelRGBA(j, i, k << 24 | 16777215);
				}
			}
		}

		RenderSystem.activeTexture(33985);
		this.texture.bind();
		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		float f = 0.06666667F;
		RenderSystem.scalef(0.06666667F, 0.06666667F, 0.06666667F);
		RenderSystem.matrixMode(5888);
		this.texture.bind();
		nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
		RenderSystem.activeTexture(33984);
	}

	public void close() {
		this.texture.close();
	}

	public void setupOverlayColor() {
		RenderSystem.setupOverlayColor(this.texture::getId, 16);
	}

	public static int u(float f) {
		return (int)(f * 15.0F);
	}

	public static int v(boolean bl) {
		return bl ? 3 : 10;
	}

	public static int pack(int i, int j) {
		return i | j << 16;
	}

	public static int pack(float f, boolean bl) {
		return pack(u(f), v(bl));
	}

	public void teardownOverlayColor() {
		RenderSystem.teardownOverlayColor();
	}
}
