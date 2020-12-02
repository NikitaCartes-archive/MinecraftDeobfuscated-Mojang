/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AnimationMetadataSectionSerializer
implements MetadataSectionSerializer<AnimationMetadataSection> {
    @Override
    public AnimationMetadataSection fromJson(JsonObject jsonObject) {
        int j;
        ArrayList<AnimationFrame> list = Lists.newArrayList();
        int i = GsonHelper.getAsInt(jsonObject, "frametime", 1);
        if (i != 1) {
            Validate.inclusiveBetween(1L, Integer.MAX_VALUE, i, "Invalid default frame time");
        }
        if (jsonObject.has("frames")) {
            try {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "frames");
                for (j = 0; j < jsonArray.size(); ++j) {
                    JsonElement jsonElement = jsonArray.get(j);
                    AnimationFrame animationFrame = this.getFrame(j, jsonElement);
                    if (animationFrame == null) continue;
                    list.add(animationFrame);
                }
            } catch (ClassCastException classCastException) {
                throw new JsonParseException("Invalid animation->frames: expected array, was " + jsonObject.get("frames"), classCastException);
            }
        }
        int k = GsonHelper.getAsInt(jsonObject, "width", -1);
        j = GsonHelper.getAsInt(jsonObject, "height", -1);
        if (k != -1) {
            Validate.inclusiveBetween(1L, Integer.MAX_VALUE, k, "Invalid width");
        }
        if (j != -1) {
            Validate.inclusiveBetween(1L, Integer.MAX_VALUE, j, "Invalid height");
        }
        boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpolate", false);
        return new AnimationMetadataSection(list, k, j, i, bl);
    }

    @Nullable
    private AnimationFrame getFrame(int i, JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return new AnimationFrame(GsonHelper.convertToInt(jsonElement, "frames[" + i + "]"));
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "frames[" + i + "]");
            int j = GsonHelper.getAsInt(jsonObject, "time", -1);
            if (jsonObject.has("time")) {
                Validate.inclusiveBetween(1L, Integer.MAX_VALUE, j, "Invalid frame time");
            }
            int k = GsonHelper.getAsInt(jsonObject, "index");
            Validate.inclusiveBetween(0L, Integer.MAX_VALUE, k, "Invalid frame index");
            return new AnimationFrame(k, j);
        }
        return null;
    }

    @Override
    public String getMetadataSectionName() {
        return "animation";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject jsonObject) {
        return this.fromJson(jsonObject);
    }
}

