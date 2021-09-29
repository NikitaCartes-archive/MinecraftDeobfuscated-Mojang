package com.mojang.realmsclient.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkinProcessor {
	private int[] pixels;
	private int width;
	private int height;

	@Nullable
	public BufferedImage process(@Nullable BufferedImage bufferedImage) {
		if (bufferedImage == null) {
			return null;
		} else {
			this.width = 64;
			this.height = 64;
			BufferedImage bufferedImage2 = new BufferedImage(this.width, this.height, 2);
			Graphics graphics = bufferedImage2.getGraphics();
			graphics.drawImage(bufferedImage, 0, 0, null);
			boolean bl = bufferedImage.getHeight() == 32;
			if (bl) {
				graphics.setColor(new Color(0, 0, 0, 0));
				graphics.fillRect(0, 32, 64, 32);
				graphics.drawImage(bufferedImage2, 24, 48, 20, 52, 4, 16, 8, 20, null);
				graphics.drawImage(bufferedImage2, 28, 48, 24, 52, 8, 16, 12, 20, null);
				graphics.drawImage(bufferedImage2, 20, 52, 16, 64, 8, 20, 12, 32, null);
				graphics.drawImage(bufferedImage2, 24, 52, 20, 64, 4, 20, 8, 32, null);
				graphics.drawImage(bufferedImage2, 28, 52, 24, 64, 0, 20, 4, 32, null);
				graphics.drawImage(bufferedImage2, 32, 52, 28, 64, 12, 20, 16, 32, null);
				graphics.drawImage(bufferedImage2, 40, 48, 36, 52, 44, 16, 48, 20, null);
				graphics.drawImage(bufferedImage2, 44, 48, 40, 52, 48, 16, 52, 20, null);
				graphics.drawImage(bufferedImage2, 36, 52, 32, 64, 48, 20, 52, 32, null);
				graphics.drawImage(bufferedImage2, 40, 52, 36, 64, 44, 20, 48, 32, null);
				graphics.drawImage(bufferedImage2, 44, 52, 40, 64, 40, 20, 44, 32, null);
				graphics.drawImage(bufferedImage2, 48, 52, 44, 64, 52, 20, 56, 32, null);
			}

			graphics.dispose();
			this.pixels = ((DataBufferInt)bufferedImage2.getRaster().getDataBuffer()).getData();
			this.setNoAlpha(0, 0, 32, 16);
			if (bl) {
				this.doLegacyTransparencyHack(32, 0, 64, 32);
			}

			this.setNoAlpha(0, 16, 64, 32);
			this.setNoAlpha(16, 48, 48, 64);
			return bufferedImage2;
		}
	}

	private void doLegacyTransparencyHack(int i, int j, int k, int l) {
		for (int m = i; m < k; m++) {
			for (int n = j; n < l; n++) {
				int o = this.pixels[m + n * this.width];
				if ((o >> 24 & 0xFF) < 128) {
					return;
				}
			}
		}

		for (int m = i; m < k; m++) {
			for (int nx = j; nx < l; nx++) {
				this.pixels[m + nx * this.width] = this.pixels[m + nx * this.width] & 16777215;
			}
		}
	}

	private void setNoAlpha(int i, int j, int k, int l) {
		for (int m = i; m < k; m++) {
			for (int n = j; n < l; n++) {
				this.pixels[m + n * this.width] = this.pixels[m + n * this.width] | 0xFF000000;
			}
		}
	}
}
