package com.mojang.blaze3d.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class SpaceProvider implements GlyphProvider {
	private final Int2ObjectMap<GlyphInfo.SpaceGlyphInfo> glyphs;

	public SpaceProvider(Int2FloatMap int2FloatMap) {
		this.glyphs = new Int2ObjectOpenHashMap<>(int2FloatMap.size());
		Int2FloatMaps.fastForEach(int2FloatMap, entry -> {
			float f = entry.getFloatValue();
			this.glyphs.put(entry.getIntKey(), () -> f);
		});
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

	public static GlyphProviderBuilder builderFromJson(JsonObject jsonObject) {
		Int2FloatMap int2FloatMap = new Int2FloatOpenHashMap();
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "advances");

		for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
			int[] is = ((String)entry.getKey()).codePoints().toArray();
			if (is.length != 1) {
				throw new JsonParseException("Expected single codepoint, got " + Arrays.toString(is));
			}

			float f = GsonHelper.convertToFloat((JsonElement)entry.getValue(), "advance");
			int2FloatMap.put(is[0], f);
		}

		GlyphProviderBuilder.Loader loader = resourceManager -> new SpaceProvider(int2FloatMap);
		return () -> Either.left(loader);
	}
}
