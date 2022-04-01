package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public enum WhiteGlyph implements RawGlyph {
	INSTANCE;

	private static final int WIDTH = 5;
	private static final int HEIGHT = 8;
	private static final NativeImage IMAGE_DATA = Util.make(new NativeImage(NativeImage.Format.RGBA, 5, 8, false), nativeImage -> {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 5; j++) {
				if (j != 0 && j + 1 != 5 && i != 0 && i + 1 != 8) {
					boolean var4 = false;
				} else {
					boolean var10000 = true;
				}

				nativeImage.setPixelRGBA(j, i, -1);
			}
		}

		nativeImage.untrack();
	});

	@Override
	public int getPixelWidth() {
		return 5;
	}

	@Override
	public int getPixelHeight() {
		return 8;
	}

	@Override
	public float getAdvance() {
		return 6.0F;
	}

	@Override
	public float getOversample() {
		return 1.0F;
	}

	@Override
	public void upload(int i, int j) {
		IMAGE_DATA.upload(0, i, j, false);
	}

	@Override
	public boolean isColored() {
		return true;
	}
}
