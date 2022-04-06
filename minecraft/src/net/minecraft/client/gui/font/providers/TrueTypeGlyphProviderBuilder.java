package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TrueTypeGlyphProviderBuilder implements GlyphProviderBuilder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ResourceLocation location;
	private final float size;
	private final float oversample;
	private final float shiftX;
	private final float shiftY;
	private final String skip;

	public TrueTypeGlyphProviderBuilder(ResourceLocation resourceLocation, float f, float g, float h, float i, String string) {
		this.location = resourceLocation;
		this.size = f;
		this.oversample = g;
		this.shiftX = h;
		this.shiftY = i;
		this.skip = string;
	}

	public static GlyphProviderBuilder fromJson(JsonObject jsonObject) {
		float f = 0.0F;
		float g = 0.0F;
		if (jsonObject.has("shift")) {
			JsonArray jsonArray = jsonObject.getAsJsonArray("shift");
			if (jsonArray.size() != 2) {
				throw new JsonParseException("Expected 2 elements in 'shift', found " + jsonArray.size());
			}

			f = GsonHelper.convertToFloat(jsonArray.get(0), "shift[0]");
			g = GsonHelper.convertToFloat(jsonArray.get(1), "shift[1]");
		}

		StringBuilder stringBuilder = new StringBuilder();
		if (jsonObject.has("skip")) {
			JsonElement jsonElement = jsonObject.get("skip");
			if (jsonElement.isJsonArray()) {
				JsonArray jsonArray2 = GsonHelper.convertToJsonArray(jsonElement, "skip");

				for (int i = 0; i < jsonArray2.size(); i++) {
					stringBuilder.append(GsonHelper.convertToString(jsonArray2.get(i), "skip[" + i + "]"));
				}
			} else {
				stringBuilder.append(GsonHelper.convertToString(jsonElement, "skip"));
			}
		}

		return new TrueTypeGlyphProviderBuilder(
			new ResourceLocation(GsonHelper.getAsString(jsonObject, "file")),
			GsonHelper.getAsFloat(jsonObject, "size", 11.0F),
			GsonHelper.getAsFloat(jsonObject, "oversample", 1.0F),
			f,
			g,
			stringBuilder.toString()
		);
	}

	@Nullable
	@Override
	public GlyphProvider create(ResourceManager resourceManager) {
		STBTTFontinfo sTBTTFontinfo = null;
		ByteBuffer byteBuffer = null;

		try {
			InputStream inputStream = resourceManager.open(new ResourceLocation(this.location.getNamespace(), "font/" + this.location.getPath()));

			TrueTypeGlyphProvider var5;
			try {
				LOGGER.debug("Loading font {}", this.location);
				sTBTTFontinfo = STBTTFontinfo.malloc();
				byteBuffer = TextureUtil.readResource(inputStream);
				byteBuffer.flip();
				LOGGER.debug("Reading font {}", this.location);
				if (!STBTruetype.stbtt_InitFont(sTBTTFontinfo, byteBuffer)) {
					throw new IOException("Invalid ttf");
				}

				var5 = new TrueTypeGlyphProvider(byteBuffer, sTBTTFontinfo, this.size, this.oversample, this.shiftX, this.shiftY, this.skip);
			} catch (Throwable var8) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var5;
		} catch (Exception var9) {
			LOGGER.error("Couldn't load truetype font {}", this.location, var9);
			if (sTBTTFontinfo != null) {
				sTBTTFontinfo.free();
			}

			MemoryUtil.memFree(byteBuffer);
			return null;
		}
	}
}
