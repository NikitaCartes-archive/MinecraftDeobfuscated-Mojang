package com.mojang.blaze3d.font;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;

@Environment(EnvType.CLIENT)
public interface GlyphInfo {
	float getAdvance();

	default float getAdvance(boolean bl) {
		return this.getAdvance() + (bl ? this.getBoldOffset() : 0.0F);
	}

	default float getBoldOffset() {
		return 1.0F;
	}

	default float getShadowOffset() {
		return 1.0F;
	}

	BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function);

	@Environment(EnvType.CLIENT)
	public interface SpaceGlyphInfo extends GlyphInfo {
		@Override
		default BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
			return EmptyGlyph.INSTANCE;
		}
	}
}
