/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.GlyphInfo;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface GlyphProvider
extends AutoCloseable {
    @Override
    default public void close() {
    }

    @Nullable
    default public GlyphInfo getGlyph(int i) {
        return null;
    }

    public IntSet getSupportedGlyphs();
}

