package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.SpaceProvider;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public enum GlyphProviderBuilderType {
	BITMAP("bitmap", BitmapProvider.Builder::fromJson),
	TTF("ttf", TrueTypeGlyphProviderBuilder::fromJson),
	SPACE("space", SpaceProvider::builderFromJson),
	UNIHEX("unihex", UnihexProvider.Builder::fromJson),
	REFERENCE("reference", ProviderReferenceBuilder::fromJson);

	private static final Map<String, GlyphProviderBuilderType> BY_NAME = Util.make(Maps.<String, GlyphProviderBuilderType>newHashMap(), hashMap -> {
		for (GlyphProviderBuilderType glyphProviderBuilderType : values()) {
			hashMap.put(glyphProviderBuilderType.name, glyphProviderBuilderType);
		}
	});
	private final String name;
	private final Function<JsonObject, GlyphProviderBuilder> factory;

	private GlyphProviderBuilderType(String string2, Function<JsonObject, GlyphProviderBuilder> function) {
		this.name = string2;
		this.factory = function;
	}

	public static GlyphProviderBuilderType byName(String string) {
		GlyphProviderBuilderType glyphProviderBuilderType = (GlyphProviderBuilderType)BY_NAME.get(string);
		if (glyphProviderBuilderType == null) {
			throw new IllegalArgumentException("Invalid type: " + string);
		} else {
			return glyphProviderBuilderType;
		}
	}

	public GlyphProviderBuilder create(JsonObject jsonObject) {
		return (GlyphProviderBuilder)this.factory.apply(jsonObject);
	}
}
