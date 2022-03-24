/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;

@Environment(value=EnvType.CLIENT)
public interface GlyphInfo {
    public float getAdvance();

    default public float getAdvance(boolean bl) {
        return this.getAdvance() + (bl ? this.getBoldOffset() : 0.0f);
    }

    default public float getBoldOffset() {
        return 1.0f;
    }

    default public float getShadowOffset() {
        return 1.0f;
    }

    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> var1);

    @Environment(value=EnvType.CLIENT)
    public static interface SpaceGlyphInfo
    extends GlyphInfo {
        @Override
        default public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return EmptyGlyph.INSTANCE;
        }
    }
}

