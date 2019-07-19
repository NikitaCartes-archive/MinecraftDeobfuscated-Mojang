/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.client.gui.font.providers.LegacyUnicodeBitmapsProvider;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderBuilder;

@Environment(value=EnvType.CLIENT)
public enum GlyphProviderBuilderType {
    BITMAP("bitmap", BitmapProvider.Builder::fromJson),
    TTF("ttf", TrueTypeGlyphProviderBuilder::fromJson),
    LEGACY_UNICODE("legacy_unicode", LegacyUnicodeBitmapsProvider.Builder::fromJson);

    private static final Map<String, GlyphProviderBuilderType> BY_NAME;
    private final String name;
    private final Function<JsonObject, GlyphProviderBuilder> factory;

    private GlyphProviderBuilderType(String string2, Function<JsonObject, GlyphProviderBuilder> function) {
        this.name = string2;
        this.factory = function;
    }

    public static GlyphProviderBuilderType byName(String string) {
        GlyphProviderBuilderType glyphProviderBuilderType = BY_NAME.get(string);
        if (glyphProviderBuilderType == null) {
            throw new IllegalArgumentException("Invalid type: " + string);
        }
        return glyphProviderBuilderType;
    }

    public GlyphProviderBuilder create(JsonObject jsonObject) {
        return this.factory.apply(jsonObject);
    }

    static {
        BY_NAME = Util.make(Maps.newHashMap(), hashMap -> {
            for (GlyphProviderBuilderType glyphProviderBuilderType : GlyphProviderBuilderType.values()) {
                hashMap.put(glyphProviderBuilderType.name, glyphProviderBuilderType);
            }
        });
    }
}

