package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;

@Environment(EnvType.CLIENT)
public class AllMissingGlyphProvider implements GlyphProvider {
	@Nullable
	@Override
	public RawGlyph getGlyph(int i) {
		return MissingGlyph.INSTANCE;
	}

	@Override
	public IntSet getSupportedGlyphs() {
		return IntSets.EMPTY_SET;
	}
}
