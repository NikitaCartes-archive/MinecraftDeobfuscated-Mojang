/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

@Environment(value=EnvType.CLIENT)
public enum SpecialGlyphs implements GlyphInfo
{
    WHITE(() -> SpecialGlyphs.generate(5, 8, (i, j) -> -1)),
    MISSING(() -> {
        int i2 = 5;
        int j2 = 8;
        return SpecialGlyphs.generate(5, 8, (i, j) -> {
            boolean bl = i == 0 || i + 1 == 5 || j == 0 || j + 1 == 8;
            return bl ? -1 : 0;
        });
    });

    final NativeImage image;

    private static NativeImage generate(int i, int j, PixelProvider pixelProvider) {
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, i, j, false);
        for (int k = 0; k < j; ++k) {
            for (int l = 0; l < i; ++l) {
                nativeImage.setPixelRGBA(l, k, pixelProvider.getColor(l, k));
            }
        }
        nativeImage.untrack();
        return nativeImage;
    }

    private SpecialGlyphs(Supplier<NativeImage> supplier) {
        this.image = supplier.get();
    }

    @Override
    public float getAdvance() {
        return this.image.getWidth() + 1;
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
        return function.apply(new SheetGlyphInfo(){

            @Override
            public int getPixelWidth() {
                return SpecialGlyphs.this.image.getWidth();
            }

            @Override
            public int getPixelHeight() {
                return SpecialGlyphs.this.image.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0f;
            }

            @Override
            public void upload(int i, int j) {
                SpecialGlyphs.this.image.upload(0, i, j, false);
            }

            @Override
            public boolean isColored() {
                return true;
            }
        });
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface PixelProvider {
        public int getColor(int var1, int var2);
    }
}

