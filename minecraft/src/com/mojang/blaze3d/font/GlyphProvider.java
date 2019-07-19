package com.mojang.blaze3d.font;

import java.io.Closeable;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GlyphProvider extends Closeable {
	default void close() {
	}

	@Nullable
	default RawGlyph getGlyph(char c) {
		return null;
	}
}
