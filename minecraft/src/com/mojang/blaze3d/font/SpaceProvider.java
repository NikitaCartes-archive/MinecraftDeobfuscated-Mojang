package com.mojang.blaze3d.font;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
public class SpaceProvider implements GlyphProvider {
	private final Int2ObjectMap<GlyphInfo.SpaceGlyphInfo> glyphs;

	public SpaceProvider(Map<Integer, Float> map) {
		this.glyphs = new Int2ObjectOpenHashMap<>(map.size());
		map.forEach((integer, float_) -> this.glyphs.put(integer.intValue(), () -> float_));
	}

	@Nullable
	@Override
	public GlyphInfo getGlyph(int i) {
		return this.glyphs.get(i);
	}

	@Override
	public IntSet getSupportedGlyphs() {
		return IntSets.unmodifiable(this.glyphs.keySet());
	}

	@Environment(EnvType.CLIENT)
	public static record Definition(Map<Integer, Float> advances) implements GlyphProviderDefinition {
		public static final MapCodec<SpaceProvider.Definition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(Codec.unboundedMap(ExtraCodecs.CODEPOINT, Codec.FLOAT).fieldOf("advances").forGetter(SpaceProvider.Definition::advances))
					.apply(instance, SpaceProvider.Definition::new)
		);

		@Override
		public GlyphProviderType type() {
			return GlyphProviderType.SPACE;
		}

		@Override
		public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
			GlyphProviderDefinition.Loader loader = resourceManager -> new SpaceProvider(this.advances);
			return Either.left(loader);
		}
	}
}
