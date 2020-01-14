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
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import net.minecraft.client.gui.font.glyphs.WhiteGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FontSet implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
	private static final GlyphInfo SPACE_INFO = () -> 4.0F;
	private static final Random RANDOM = new Random();
	private final TextureManager textureManager;
	private final ResourceLocation name;
	private BakedGlyph missingGlyph;
	private BakedGlyph whiteGlyph;
	private final List<GlyphProvider> providers = Lists.<GlyphProvider>newArrayList();
	private final Char2ObjectMap<BakedGlyph> glyphs = new Char2ObjectOpenHashMap<>();
	private final Char2ObjectMap<GlyphInfo> glyphInfos = new Char2ObjectOpenHashMap<>();
	private final Int2ObjectMap<CharList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
	private final List<FontTexture> textures = Lists.<FontTexture>newArrayList();

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
		Set<GlyphProvider> set = Sets.<GlyphProvider>newHashSet();

		for (char c = 0; c < '\uffff'; c++) {
			for (GlyphProvider glyphProvider : list) {
				GlyphInfo glyphInfo = (GlyphInfo)(c == ' ' ? SPACE_INFO : glyphProvider.getGlyph(c));
				if (glyphInfo != null) {
					set.add(glyphProvider);
					if (glyphInfo != MissingGlyph.INSTANCE) {
						this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphInfo.getAdvance(false)), i -> new CharArrayList()).add(c);
					}
					break;
				}
			}
		}

		list.stream().filter(set::contains).forEach(this.providers::add);
	}

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

	public GlyphInfo getGlyphInfo(char c) {
		return this.glyphInfos.computeIfAbsent(c, i -> (GlyphInfo)(i == 32 ? SPACE_INFO : this.getRaw((char)i)));
	}

	private RawGlyph getRaw(char c) {
		for (GlyphProvider glyphProvider : this.providers) {
			RawGlyph rawGlyph = glyphProvider.getGlyph(c);
			if (rawGlyph != null) {
				return rawGlyph;
			}
		}

		return MissingGlyph.INSTANCE;
	}

	public BakedGlyph getGlyph(char c) {
		return this.glyphs.computeIfAbsent(c, i -> (BakedGlyph)(i == 32 ? SPACE_GLYPH : this.stitch(this.getRaw((char)i))));
	}

	private BakedGlyph stitch(RawGlyph rawGlyph) {
		for (FontTexture fontTexture : this.textures) {
			BakedGlyph bakedGlyph = fontTexture.add(rawGlyph);
			if (bakedGlyph != null) {
				return bakedGlyph;
			}
		}

		FontTexture fontTexture2 = new FontTexture(
			new ResourceLocation(this.name.getNamespace(), this.name.getPath() + "/" + this.textures.size()), rawGlyph.isColored()
		);
		this.textures.add(fontTexture2);
		this.textureManager.register(fontTexture2.getName(), fontTexture2);
		BakedGlyph bakedGlyph2 = fontTexture2.add(rawGlyph);
		return bakedGlyph2 == null ? this.missingGlyph : bakedGlyph2;
	}

	public BakedGlyph getRandomGlyph(GlyphInfo glyphInfo) {
		CharList charList = this.glyphsByWidth.get(Mth.ceil(glyphInfo.getAdvance(false)));
		return charList != null && !charList.isEmpty() ? this.getGlyph(charList.get(RANDOM.nextInt(charList.size()))) : this.missingGlyph;
	}

	public BakedGlyph whiteGlyph() {
		return this.whiteGlyph;
	}
}
