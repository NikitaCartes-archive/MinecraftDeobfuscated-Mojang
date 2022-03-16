/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpaceProvider
implements GlyphProvider {
    static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
    private final Int2ObjectMap<SpaceGlyphInfo> glyphs;

    public SpaceProvider(Int2FloatMap int2FloatMap) {
        this.glyphs = new Int2ObjectOpenHashMap<SpaceGlyphInfo>(int2FloatMap.size());
        Int2FloatMaps.fastForEach(int2FloatMap, entry -> {
            float f = entry.getFloatValue();
            this.glyphs.put(entry.getIntKey(), () -> f);
        });
    }

    @Override
    @Nullable
    public GlyphInfo getGlyph(int i) {
        return (GlyphInfo)this.glyphs.get(i);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    public static GlyphProviderBuilder builderFromJson(JsonObject jsonObject) {
        Int2FloatOpenHashMap int2FloatMap = new Int2FloatOpenHashMap();
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "advances");
        for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
            int[] is = entry.getKey().codePoints().toArray();
            if (is.length != 1) {
                throw new JsonParseException("Expected single codepoint, got " + Arrays.toString(is));
            }
            float f = GsonHelper.convertToFloat(entry.getValue(), "advance");
            int2FloatMap.put(is[0], f);
        }
        return resourceManager -> new SpaceProvider(int2FloatMap);
    }

    @Environment(value=EnvType.CLIENT)
    static interface SpaceGlyphInfo
    extends GlyphInfo {
        @Override
        default public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return SPACE_GLYPH;
        }
    }
}

