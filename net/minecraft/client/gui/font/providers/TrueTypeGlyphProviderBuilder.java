/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TrueTypeGlyphProviderBuilder
implements GlyphProviderBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
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
        float f = 0.0f;
        float g = 0.0f;
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
                for (int i = 0; i < jsonArray2.size(); ++i) {
                    stringBuilder.append(GsonHelper.convertToString(jsonArray2.get(i), "skip[" + i + "]"));
                }
            } else {
                stringBuilder.append(GsonHelper.convertToString(jsonElement, "skip"));
            }
        }
        return new TrueTypeGlyphProviderBuilder(new ResourceLocation(GsonHelper.getAsString(jsonObject, "file")), GsonHelper.getAsFloat(jsonObject, "size", 11.0f), GsonHelper.getAsFloat(jsonObject, "oversample", 1.0f), f, g, stringBuilder.toString());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    @Nullable
    public GlyphProvider create(ResourceManager resourceManager) {
        try (Resource resource = resourceManager.getResource(new ResourceLocation(this.location.getNamespace(), "font/" + this.location.getPath()));){
            LOGGER.info("Loading font");
            ByteBuffer byteBuffer = TextureUtil.readResource(resource.getInputStream());
            byteBuffer.flip();
            LOGGER.info("Reading font");
            TrueTypeGlyphProvider trueTypeGlyphProvider = new TrueTypeGlyphProvider(TrueTypeGlyphProvider.getStbttFontinfo(byteBuffer), this.size, this.oversample, this.shiftX, this.shiftY, this.skip);
            return trueTypeGlyphProvider;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't load truetype font {}", (Object)this.location, (Object)iOException);
            return null;
        }
    }
}

