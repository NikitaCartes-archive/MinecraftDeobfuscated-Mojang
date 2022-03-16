package com.mojang.blaze3d.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SheetGlyphInfo {
	int getPixelWidth();

	int getPixelHeight();

	void upload(int i, int j);

	boolean isColored();

	float getOversample();

	default float getLeft() {
		return this.getBearingX();
	}

	default float getRight() {
		return this.getLeft() + (float)this.getPixelWidth() / this.getOversample();
	}

	default float getUp() {
		return this.getBearingY();
	}

	default float getDown() {
		return this.getUp() + (float)this.getPixelHeight() / this.getOversample();
	}

	default float getBearingX() {
		return 0.0F;
	}

	default float getBearingY() {
		return 3.0F;
	}
}
