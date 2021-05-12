package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class BlockFaceUV {
	public float[] uvs;
	public final int rotation;

	public BlockFaceUV(@Nullable float[] fs, int i) {
		this.uvs = fs;
		this.rotation = i;
	}

	public float getU(int i) {
		if (this.uvs == null) {
			throw new NullPointerException("uvs");
		} else {
			int j = this.getShiftedIndex(i);
			return this.uvs[j != 0 && j != 1 ? 2 : 0];
		}
	}

	public float getV(int i) {
		if (this.uvs == null) {
			throw new NullPointerException("uvs");
		} else {
			int j = this.getShiftedIndex(i);
			return this.uvs[j != 0 && j != 3 ? 3 : 1];
		}
	}

	private int getShiftedIndex(int i) {
		return (i + this.rotation / 90) % 4;
	}

	public int getReverseIndex(int i) {
		return (i + 4 - this.rotation / 90) % 4;
	}

	public void setMissingUv(float[] fs) {
		if (this.uvs == null) {
			this.uvs = fs;
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class Deserializer implements JsonDeserializer<BlockFaceUV> {
		private static final int DEFAULT_ROTATION = 0;

		public BlockFaceUV deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			float[] fs = this.getUVs(jsonObject);
			int i = this.getRotation(jsonObject);
			return new BlockFaceUV(fs, i);
		}

		protected int getRotation(JsonObject jsonObject) {
			int i = GsonHelper.getAsInt(jsonObject, "rotation", 0);
			if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
				return i;
			} else {
				throw new JsonParseException("Invalid rotation " + i + " found, only 0/90/180/270 allowed");
			}
		}

		@Nullable
		private float[] getUVs(JsonObject jsonObject) {
			if (!jsonObject.has("uv")) {
				return null;
			} else {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "uv");
				if (jsonArray.size() != 4) {
					throw new JsonParseException("Expected 4 uv values, found: " + jsonArray.size());
				} else {
					float[] fs = new float[4];

					for (int i = 0; i < fs.length; i++) {
						fs[i] = GsonHelper.convertToFloat(jsonArray.get(i), "uv[" + i + "]");
					}

					return fs;
				}
			}
		}
	}
}
