package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;

@Environment(EnvType.CLIENT)
public class AllMissingGlyphProvider implements GlyphProvider {
	@Nullable
	@Override
	public RawGlyph getGlyph(char c) {
		return MissingGlyph.INSTANCE;
	}
}
