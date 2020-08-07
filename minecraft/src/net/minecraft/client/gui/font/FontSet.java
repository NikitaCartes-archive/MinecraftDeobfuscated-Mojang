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
import it.unimi.dsi.fastutil.ints.IntSet;
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

@Environment(EnvType.CLIENT)
public class FontSet implements AutoCloseable {
	private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
	private static final GlyphInfo SPACE_INFO = () -> 4.0F;
	private static final Random RANDOM = new Random();
	private final TextureManager textureManager;
	private final ResourceLocation name;
	private BakedGlyph missingGlyph;
	private BakedGlyph whiteGlyph;
	private final List<GlyphProvider> providers = Lists.<GlyphProvider>newArrayList();
	private final Int2ObjectMap<BakedGlyph> glyphs = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<GlyphInfo> glyphInfos = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
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
		IntSet intSet = new IntOpenHashSet();

		for (GlyphProvider glyphProvider : list) {
			intSet.addAll(glyphProvider.getSupportedGlyphs());
		}

		Set<GlyphProvider> set = Sets.<GlyphProvider>newHashSet();
		intSet.forEach(i -> {
			for (GlyphProvider glyphProviderx : list) {
				GlyphInfo glyphInfo = (GlyphInfo)(i == 32 ? SPACE_INFO : glyphProviderx.getGlyph(i));
				if (glyphInfo != null) {
					set.add(glyphProviderx);
					if (glyphInfo != MissingGlyph.INSTANCE) {
						this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphInfo.getAdvance(false)), ix -> new IntArrayList()).add(i);
					}
					break;
				}
			}
		});
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

	public GlyphInfo getGlyphInfo(int i) {
		return this.glyphInfos.computeIfAbsent(i, ix -> (GlyphInfo)(ix == 32 ? SPACE_INFO : this.getRaw(ix)));
	}

	private RawGlyph getRaw(int i) {
		for (GlyphProvider glyphProvider : this.providers) {
			RawGlyph rawGlyph = glyphProvider.getGlyph(i);
			if (rawGlyph != null) {
				return rawGlyph;
			}
		}

		return MissingGlyph.INSTANCE;
	}

	public BakedGlyph getGlyph(int i) {
		return this.glyphs.computeIfAbsent(i, ix -> (BakedGlyph)(ix == 32 ? SPACE_GLYPH : this.stitch(this.getRaw(ix))));
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
		IntList intList = this.glyphsByWidth.get(Mth.ceil(glyphInfo.getAdvance(false)));
		return intList != null && !intList.isEmpty() ? this.getGlyph(intList.getInt(RANDOM.nextInt(intList.size()))) : this.missingGlyph;
	}

	public BakedGlyph whiteGlyph() {
		return this.whiteGlyph;
	}
}
