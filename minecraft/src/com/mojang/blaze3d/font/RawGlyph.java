package com.mojang.blaze3d.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RawGlyph extends GlyphInfo {
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

	@Override
	default float getBearingY() {
		return 3.0F;
	}
}
