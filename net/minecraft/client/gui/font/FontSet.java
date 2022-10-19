/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.HashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class FontSet
implements AutoCloseable {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final float LARGE_FORWARD_ADVANCE = 32.0f;
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> providers = Lists.newArrayList();
    private final Int2ObjectMap<BakedGlyph> glyphs = new Int2ObjectOpenHashMap<BakedGlyph>();
    private final Int2ObjectMap<GlyphInfoFilter> glyphInfos = new Int2ObjectOpenHashMap<GlyphInfoFilter>();
    private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<IntList>();
    private final List<FontTexture> textures = Lists.newArrayList();

    public FontSet(TextureManager textureManager, ResourceLocation resourceLocation) {
        this.textureManager = textureManager;
        this.name = resourceLocation;
    }

    public void reload(List<GlyphProvider> list) {
        this.closeProviders();
        this.closeTextures();
        this.glyphs.clear();
        this.glyphInfos.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = SpecialGlyphs.MISSING.bake(this::stitch);
        this.whiteGlyph = SpecialGlyphs.WHITE.bake(this::stitch);
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (GlyphProvider glyphProvider : list) {
            intSet.addAll(glyphProvider.getSupportedGlyphs());
        }
        HashSet set = Sets.newHashSet();
        intSet.forEach(i2 -> {
            for (GlyphProvider glyphProvider : list) {
                GlyphInfo glyphInfo = glyphProvider.getGlyph(i2);
                if (glyphInfo == null) continue;
                set.add(glyphProvider);
                if (glyphInfo == SpecialGlyphs.MISSING) break;
                this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphInfo.getAdvance(false)), i -> new IntArrayList()).add(i2);
                break;
            }
        });
        list.stream().filter(set::contains).forEach(this.providers::add);
    }

    @Override
    public void close() {
        this.closeProviders();
        this.closeTextures();
    }

    private void closeProviders() {
        for (GlyphProvider glyphProvider : this.providers) {
            glyphProvider.close();
        }
        this.providers.clear();
    }

    private void closeTextures() {
        for (FontTexture fontTexture : this.textures) {
            fontTexture.close();
        }
        this.textures.clear();
    }

    private static boolean hasFishyAdvance(GlyphInfo glyphInfo) {
        float f = glyphInfo.getAdvance(false);
        if (f < 0.0f || f > 32.0f) {
            return true;
        }
        float g = glyphInfo.getAdvance(true);
        return g < 0.0f || g > 32.0f;
    }

    private GlyphInfoFilter computeGlyphInfo(int i) {
        GlyphInfo glyphInfo = null;
        for (GlyphProvider glyphProvider : this.providers) {
            GlyphInfo glyphInfo2 = glyphProvider.getGlyph(i);
            if (glyphInfo2 == null) continue;
            if (glyphInfo == null) {
                glyphInfo = glyphInfo2;
            }
            if (FontSet.hasFishyAdvance(glyphInfo2)) continue;
            return new GlyphInfoFilter(glyphInfo, glyphInfo2);
        }
        if (glyphInfo != null) {
            return new GlyphInfoFilter(glyphInfo, SpecialGlyphs.MISSING);
        }
        return GlyphInfoFilter.MISSING;
    }

    public GlyphInfo getGlyphInfo(int i, boolean bl) {
        return this.glyphInfos.computeIfAbsent(i, this::computeGlyphInfo).select(bl);
    }

    private BakedGlyph computeBakedGlyph(int i) {
        for (GlyphProvider glyphProvider : this.providers) {
            GlyphInfo glyphInfo = glyphProvider.getGlyph(i);
            if (glyphInfo == null) continue;
            return glyphInfo.bake(this::stitch);
        }
        return this.missingGlyph;
    }

    public BakedGlyph getGlyph(int i) {
        return this.glyphs.computeIfAbsent(i, this::computeBakedGlyph);
    }

    private BakedGlyph stitch(SheetGlyphInfo sheetGlyphInfo) {
        for (FontTexture fontTexture : this.textures) {
            BakedGlyph bakedGlyph = fontTexture.add(sheetGlyphInfo);
            if (bakedGlyph == null) continue;
            return bakedGlyph;
        }
        FontTexture fontTexture2 = new FontTexture(this.name.withPath(string -> string + "/" + this.textures.size()), sheetGlyphInfo.isColored());
        this.textures.add(fontTexture2);
        this.textureManager.register(fontTexture2.getName(), fontTexture2);
        BakedGlyph bakedGlyph2 = fontTexture2.add(sheetGlyphInfo);
        return bakedGlyph2 == null ? this.missingGlyph : bakedGlyph2;
    }

    public BakedGlyph getRandomGlyph(GlyphInfo glyphInfo) {
        IntList intList = (IntList)this.glyphsByWidth.get(Mth.ceil(glyphInfo.getAdvance(false)));
        if (intList != null && !intList.isEmpty()) {
            return this.getGlyph(intList.getInt(RANDOM.nextInt(intList.size())));
        }
        return this.missingGlyph;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }

    @Environment(value=EnvType.CLIENT)
    record GlyphInfoFilter(GlyphInfo glyphInfo, GlyphInfo glyphInfoNotFishy) {
        static final GlyphInfoFilter MISSING = new GlyphInfoFilter(SpecialGlyphs.MISSING, SpecialGlyphs.MISSING);

        GlyphInfo select(boolean bl) {
            return bl ? this.glyphInfoNotFishy : this.glyphInfo;
        }
    }
}

