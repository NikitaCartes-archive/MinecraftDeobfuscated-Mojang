package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.damagesource.DamageSource;

@Environment(EnvType.CLIENT)
public class OverlayTexture implements AutoCloseable {
	public static final int NO_OVERLAY = pack(0, 10);
	private final DynamicTexture texture = new DynamicTexture(24, 24, false);

	public OverlayTexture() {
		NativeImage nativeImage = this.texture.getPixels();

		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 24; j++) {
				if (i < 8) {
					nativeImage.setPixelRGBA(j, i, -1308622593);
				} else if (i < 16) {
					int k = (int)((1.0F - (float)j / 15.0F * 0.75F) * 255.0F);
					nativeImage.setPixelRGBA(j, i, k << 24 | 16777215);
				} else {
					nativeImage.setPixelRGBA(j, i, -1291911168);
				}
			}
		}

		RenderSystem.activeTexture(33985);
		this.texture.bind();
		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		float f = 0.04347826F;
		RenderSystem.scalef(0.04347826F, 0.04347826F, 0.04347826F);
		RenderSystem.matrixMode(5888);
		this.texture.bind();
		nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
		RenderSystem.activeTexture(33984);
	}

	public void close() {
		this.texture.close();
	}

	public void setupOverlayColor() {
		RenderSystem.setupOverlayColor(this.texture::getId, 24);
	}

	public static int u(float f) {
		return (int)(f * 23.0F);
	}

	public static int v(boolean bl) {
		return v(bl, null);
	}

	public static int v(boolean bl, @Nullable DamageSource damageSource) {
		if (bl) {
			return damageSource == DamageSource.FREEZE ? 19 : 3;
		} else {
			return 10;
		}
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
