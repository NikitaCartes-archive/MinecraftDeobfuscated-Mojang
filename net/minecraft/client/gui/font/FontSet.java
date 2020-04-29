/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import net.minecraft.client.gui.font.glyphs.WhiteGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class FontSet
implements AutoCloseable {
    private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
    private static final GlyphInfo SPACE_INFO = () -> 4.0f;
    private static final Random RANDOM = new Random();
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> providers = Lists.newArrayList();
    private final Int2ObjectMap<BakedGlyph> glyphs = new Int2ObjectOpenHashMap<BakedGlyph>();
    private final Int2ObjectMap<GlyphInfo> glyphInfos = new Int2ObjectOpenHashMap<GlyphInfo>();
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
        this.missingGlyph = this.stitch(MissingGlyph.INSTANCE);
        this.whiteGlyph = this.stitch(WhiteGlyph.INSTANCE);
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (GlyphProvider glyphProvider : list) {
            intSet.addAll(glyphProvider.getSupportedGlyphs());
        }
        HashSet set = Sets.newHashSet();
        intSet.forEach(i2 -> {
            for (GlyphProvider glyphProvider : list) {
                GlyphInfo glyphInfo = i2 == 32 ? SPACE_INFO : glyphProvider.getGlyph(i2);
                if (glyphInfo == null) continue;
                set.add(glyphProvider);
                if (glyphInfo == MissingGlyph.INSTANCE) break;
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

    public GlyphInfo getGlyphInfo(int i2) {
        return this.glyphInfos.computeIfAbsent(i2, i -> i == 32 ? SPACE_INFO : this.getRaw(i));
    }

    private RawGlyph getRaw(int i) {
        for (GlyphProvider glyphProvider : this.providers) {
            RawGlyph rawGlyph = glyphProvider.getGlyph(i);
            if (rawGlyph == null) continue;
            return rawGlyph;
        }
        return MissingGlyph.INSTANCE;
    }

    public BakedGlyph getGlyph(int i2) {
        return this.glyphs.computeIfAbsent(i2, i -> i == 32 ? SPACE_GLYPH : this.stitch(this.getRaw(i)));
    }

    private BakedGlyph stitch(RawGlyph rawGlyph) {
        for (FontTexture fontTexture : this.textures) {
            BakedGlyph bakedGlyph = fontTexture.add(rawGlyph);
            if (bakedGlyph == null) continue;
            return bakedGlyph;
        }
        FontTexture fontTexture2 = new FontTexture(new ResourceLocation(this.name.getNamespace(), this.name.getPath() + "/" + this.textures.size()), rawGlyph.isColored());
        this.textures.add(fontTexture2);
        this.textureManager.register(fontTexture2.getName(), fontTexture2);
        BakedGlyph bakedGlyph2 = fontTexture2.add(rawGlyph);
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
}

