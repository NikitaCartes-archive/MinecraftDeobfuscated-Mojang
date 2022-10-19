package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public class MipmapGenerator {
	private static final int ALPHA_CUTOUT_CUTOFF = 96;
	private static final float[] POW22 = Util.make(new float[256], fs -> {
		for (int i = 0; i < fs.length; i++) {
			fs[i] = (float)Math.pow((double)((float)i / 255.0F), 2.2);
		}
	});

	private MipmapGenerator() {
	}

	public static NativeImage[] generateMipLevels(NativeImage[] nativeImages, int i) {
		if (i + 1 <= nativeImages.length) {
			return nativeImages;
		} else {
			NativeImage[] nativeImages2 = new NativeImage[i + 1];
			nativeImages2[0] = nativeImages[0];
			boolean bl = hasTransparentPixel(nativeImages2[0]);

			for (int j = 1; j <= i; j++) {
				if (j < nativeImages.length) {
					nativeImages2[j] = nativeImages[j];
				} else {
					NativeImage nativeImage = nativeImages2[j - 1];
					NativeImage nativeImage2 = new NativeImage(nativeImage.getWidth() >> 1, nativeImage.getHeight() >> 1, false);
					int k = nativeImage2.getWidth();
					int l = nativeImage2.getHeight();

					for (int m = 0; m < k; m++) {
						for (int n = 0; n < l; n++) {
							nativeImage2.setPixelRGBA(
								m,
								n,
								alphaBlend(
									nativeImage.getPixelRGBA(m * 2 + 0, n * 2 + 0),
									nativeImage.getPixelRGBA(m * 2 + 1, n * 2 + 0),
									nativeImage.getPixelRGBA(m * 2 + 0, n * 2 + 1),
									nativeImage.getPixelRGBA(m * 2 + 1, n * 2 + 1),
									bl
								)
							);
						}
					}

					nativeImages2[j] = nativeImage2;
				}
			}

			return nativeImages2;
		}
	}

	private static boolean hasTransparentPixel(NativeImage nativeImage) {
		for (int i = 0; i < nativeImage.getWidth(); i++) {
			for (int j = 0; j < nativeImage.getHeight(); j++) {
				if (nativeImage.getPixelRGBA(i, j) >> 24 == 0) {
					return true;
				}
			}
		}

		return false;
	}

	private static int alphaBlend(int i, int j, int k, int l, boolean bl) {
		if (bl) {
			float f = 0.0F;
			float g = 0.0F;
			float h = 0.0F;
			float m = 0.0F;
			if (i >> 24 != 0) {
				f += getPow22(i >> 24);
				g += getPow22(i >> 16);
				h += getPow22(i >> 8);
				m += getPow22(i >> 0);
			}

			if (j >> 24 != 0) {
				f += getPow22(j >> 24);
				g += getPow22(j >> 16);
				h += getPow22(j >> 8);
				m += getPow22(j >> 0);
			}

			if (k >> 24 != 0) {
				f += getPow22(k >> 24);
				g += getPow22(k >> 16);
				h += getPow22(k >> 8);
				m += getPow22(k >> 0);
			}

			if (l >> 24 != 0) {
				f += getPow22(l >> 24);
				g += getPow22(l >> 16);
				h += getPow22(l >> 8);
				m += getPow22(l >> 0);
			}

			f /= 4.0F;
			g /= 4.0F;
			h /= 4.0F;
			m /= 4.0F;
			int n = (int)(Math.pow((double)f, 0.45454545454545453) * 255.0);
			int o = (int)(Math.pow((double)g, 0.45454545454545453) * 255.0);
			int p = (int)(Math.pow((double)h, 0.45454545454545453) * 255.0);
			int q = (int)(Math.pow((double)m, 0.45454545454545453) * 255.0);
			if (n < 96) {
				n = 0;
			}

			return n << 24 | o << 16 | p << 8 | q;
		} else {
			int r = gammaBlend(i, j, k, l, 24);
			int s = gammaBlend(i, j, k, l, 16);
			int t = gammaBlend(i, j, k, l, 8);
			int u = gammaBlend(i, j, k, l, 0);
			return r << 24 | s << 16 | t << 8 | u;
		}
	}

	private static int gammaBlend(int i, int j, int k, int l, int m) {
		float f = getPow22(i >> m);
		float g = getPow22(j >> m);
		float h = getPow22(k >> m);
		float n = getPow22(l >> m);
		float o = (float)((double)((float)Math.pow((double)(f + g + h + n) * 0.25, 0.45454545454545453)));
		return (int)((double)o * 255.0);
	}

	private static float getPow22(int i) {
		return POW22[i & 0xFF];
	}
}
