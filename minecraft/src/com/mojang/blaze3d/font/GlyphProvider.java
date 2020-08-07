package com.mojang.blaze3d.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Closeable;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GlyphProvider extends Closeable {
	default void close() {
	}

	@Nullable
	default RawGlyph getGlyph(int i) {
		return null;
	}

	IntSet getSupportedGlyphs();
}
