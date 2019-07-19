package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.Validate;

@Environment(EnvType.CLIENT)
public class AnimationMetadataSectionSerializer implements MetadataSectionSerializer<AnimationMetadataSection> {
	public AnimationMetadataSection fromJson(JsonObject jsonObject) {
		List<AnimationFrame> list = Lists.<AnimationFrame>newArrayList();
		int i = GsonHelper.getAsInt(jsonObject, "frametime", 1);
		if (i != 1) {
			Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid default frame time");
		}

		if (jsonObject.has("frames")) {
			try {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "frames");

				for (int j = 0; j < jsonArray.size(); j++) {
					JsonElement jsonElement = jsonArray.get(j);
					AnimationFrame animationFrame = this.getFrame(j, jsonElement);
					if (animationFrame != null) {
						list.add(animationFrame);
					}
				}
			} catch (ClassCastException var8) {
				throw new JsonParseException("Invalid animation->frames: expected array, was " + jsonObject.get("frames"), var8);
			}
		}

		int k = GsonHelper.getAsInt(jsonObject, "width", -1);
		int jx = GsonHelper.getAsInt(jsonObject, "height", -1);
		if (k != -1) {
			Validate.inclusiveBetween(1L, 2147483647L, (long)k, "Invalid width");
		}

		if (jx != -1) {
			Validate.inclusiveBetween(1L, 2147483647L, (long)jx, "Invalid height");
		}

		boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpolate", false);
		return new AnimationMetadataSection(list, k, jx, i, bl);
	}

	private AnimationFrame getFrame(int i, JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			return new AnimationFrame(GsonHelper.convertToInt(jsonElement, "frames[" + i + "]"));
		} else if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "frames[" + i + "]");
			int j = GsonHelper.getAsInt(jsonObject, "time", -1);
			if (jsonObject.has("time")) {
				Validate.inclusiveBetween(1L, 2147483647L, (long)j, "Invalid frame time");
			}

			int k = GsonHelper.getAsInt(jsonObject, "index");
			Validate.inclusiveBetween(0L, 2147483647L, (long)k, "Invalid frame index");
			return new AnimationFrame(k, j);
		} else {
			return null;
		}
	}

	@Override
	public String getMetadataSectionName() {
		return "animation";
	}
}
