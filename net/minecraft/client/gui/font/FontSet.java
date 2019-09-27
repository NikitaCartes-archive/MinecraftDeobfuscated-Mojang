/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FontSet
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
    private static final GlyphInfo SPACE_INFO = () -> 4.0f;
    private static final Random RANDOM = new Random();
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> providers = Lists.newArrayList();
    private final Char2ObjectMap<BakedGlyph> glyphs = new Char2ObjectOpenHashMap<BakedGlyph>();
    private final Char2ObjectMap<GlyphInfo> glyphInfos = new Char2ObjectOpenHashMap<GlyphInfo>();
    private final Int2ObjectMap<CharList> glyphsByWidth = new Int2ObjectOpenHashMap<CharList>();
    private final List<FontTexture> textures = Lists.newArrayList();

    public FontSet(TextureManager textureManager, ResourceLocation resourceLocation) {
        this.textureManager = textureManager;
        this.name = resourceLocation;
    }

    public void reload(List<GlyphProvider> list) {
        for (GlyphProvider glyphProvider : this.providers) {
            glyphProvider.close();
        }
        this.providers.clear();
        this.closeTextures();
        this.textures.clear();
        this.glyphs.clear();
        this.glyphInfos.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = this.stitch(MissingGlyph.INSTANCE);
        this.whiteGlyph = this.stitch(WhiteGlyph.INSTANCE);
        HashSet<GlyphProvider> set = Sets.newHashSet();
        block1: for (char c = '\u0000'; c < '\uffff'; c = (char)((char)(c + 1))) {
            for (GlyphProvider glyphProvider2 : list) {
                GlyphInfo glyphInfo = c == ' ' ? SPACE_INFO : glyphProvider2.getGlyph(c);
                if (glyphInfo == null) continue;
                set.add(glyphProvider2);
                if (glyphInfo == MissingGlyph.INSTANCE) continue block1;
                this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphInfo.getAdvance(false)), i -> new CharArrayList()).add(c);
                continue block1;
            }
        }
        list.stream().filter(set::contains).forEach(this.providers::add);
    }

    @Override
    public void close() {
        this.closeTextures();
    }

    public void closeTextures() {
        for (FontTexture fontTexture : this.textures) {
            fontTexture.close();
        }
    }

    public GlyphInfo getGlyphInfo(char c) {
        return this.glyphInfos.computeIfAbsent(c, i -> i == 32 ? SPACE_INFO : this.getRaw((char)i));
    }

    private RawGlyph getRaw(char c) {
        for (GlyphProvider glyphProvider : this.providers) {
            RawGlyph rawGlyph = glyphProvider.getGlyph(c);
            if (rawGlyph == null) continue;
            return rawGlyph;
        }
        return MissingGlyph.INSTANCE;
    }

    public BakedGlyph getGlyph(char c) {
        return this.glyphs.computeIfAbsent(c, i -> i == 32 ? SPACE_GLYPH : this.stitch(this.getRaw((char)i)));
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
        CharList charList = (CharList)this.glyphsByWidth.get(Mth.ceil(glyphInfo.getAdvance(false)));
        if (charList != null && !charList.isEmpty()) {
            return this.getGlyph(charList.get(RANDOM.nextInt(charList.size())).charValue());
        }
        return this.missingGlyph;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }
}

