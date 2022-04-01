package com.mojang.blaze3d.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GlyphInfo {
	float getAdvance();

	default float getAdvance(boolean bl) {
		return this.getAdvance() + (bl ? this.getBoldOffset() : 0.0F);
	}

	default float getBearingX() {
		return 0.0F;
	}

	default float getBearingY() {
		return 0.0F;
	}

	default float getBoldOffset() {
		return 1.0F;
	}

	default float getShadowOffset() {
		return 1.0F;
	}
}
