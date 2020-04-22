/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AllMissingGlyphProvider
implements GlyphProvider {
    @Override
    @Nullable
    public RawGlyph getGlyph(int i) {
        return MissingGlyph.INSTANCE;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.EMPTY_SET;
    }
}

