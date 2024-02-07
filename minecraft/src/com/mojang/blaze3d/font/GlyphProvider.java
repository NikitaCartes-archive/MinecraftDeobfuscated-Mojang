package com.mojang.blaze3d.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.FontOption;

@Environment(EnvType.CLIENT)
public interface GlyphProvider extends AutoCloseable {
	float BASELINE = 7.0F;

	default void close() {
	}

	@Nullable
	default GlyphInfo getGlyph(int i) {
		return null;
	}

	IntSet getSupportedGlyphs();

	@Environment(EnvType.CLIENT)
	public static record Conditional(GlyphProvider provider, FontOption.Filter filter) implements AutoCloseable {
		public void close() {
			this.provider.close();
		}
	}
}
