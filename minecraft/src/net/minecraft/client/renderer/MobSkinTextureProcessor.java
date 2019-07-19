package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MobSkinTextureProcessor implements HttpTextureProcessor {
	@Override
	public NativeImage process(NativeImage nativeImage) {
		boolean bl = nativeImage.getHeight() == 32;
		if (bl) {
			NativeImage nativeImage2 = new NativeImage(64, 64, true);
			nativeImage2.copyFrom(nativeImage);
			nativeImage.close();
			nativeImage = nativeImage2;
			nativeImage2.fillRect(0, 32, 64, 32, 0);
			nativeImage2.copyRect(4, 16, 16, 32, 4, 4, true, false);
			nativeImage2.copyRect(8, 16, 16, 32, 4, 4, true, false);
			nativeImage2.copyRect(0, 20, 24, 32, 4, 12, true, false);
			nativeImage2.copyRect(4, 20, 16, 32, 4, 12, true, false);
			nativeImage2.copyRect(8, 20, 8, 32, 4, 12, true, false);
			nativeImage2.copyRect(12, 20, 16, 32, 4, 12, true, false);
			nativeImage2.copyRect(44, 16, -8, 32, 4, 4, true, false);
			nativeImage2.copyRect(48, 16, -8, 32, 4, 4, true, false);
			nativeImage2.copyRect(40, 20, 0, 32, 4, 12, true, false);
			nativeImage2.copyRect(44, 20, -8, 32, 4, 12, true, false);
			nativeImage2.copyRect(48, 20, -16, 32, 4, 12, true, false);
			nativeImage2.copyRect(52, 20, -8, 32, 4, 12, true, false);
		}

		setNoAlpha(nativeImage, 0, 0, 32, 16);
		if (bl) {
			doLegacyTransparencyHack(nativeImage, 32, 0, 64, 32);
		}

		setNoAlpha(nativeImage, 0, 16, 64, 32);
		setNoAlpha(nativeImage, 16, 48, 48, 64);
		return nativeImage;
	}

	@Override
	public void onTextureDownloaded() {
	}

	private static void doLegacyTransparencyHack(NativeImage nativeImage, int i, int j, int k, int l) {
		for (int m = i; m < k; m++) {
			for (int n = j; n < l; n++) {
				int o = nativeImage.getPixelRGBA(m, n);
				if ((o >> 24 & 0xFF) < 128) {
					return;
				}
			}
		}

		for (int m = i; m < k; m++) {
			for (int nx = j; nx < l; nx++) {
				nativeImage.setPixelRGBA(m, nx, nativeImage.getPixelRGBA(m, nx) & 16777215);
			}
		}
	}

	private static void setNoAlpha(NativeImage nativeImage, int i, int j, int k, int l) {
		for (int m = i; m < k; m++) {
			for (int n = j; n < l; n++) {
				nativeImage.setPixelRGBA(m, n, nativeImage.getPixelRGBA(m, n) | 0xFF000000);
			}
		}
	}
}
