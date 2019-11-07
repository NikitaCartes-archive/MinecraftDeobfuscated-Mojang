package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class ItemTransform {
	public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
	public final Vector3f rotation;
	public final Vector3f translation;
	public final Vector3f scale;

	public ItemTransform(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
		this.rotation = vector3f.copy();
		this.translation = vector3f2.copy();
		this.scale = vector3f3.copy();
	}

	public void apply(boolean bl, PoseStack poseStack) {
		if (this != NO_TRANSFORM) {
			float f = this.rotation.x();
			float g = this.rotation.y();
			float h = this.rotation.z();
			if (bl) {
				g = -g;
				h = -h;
			}

			int i = bl ? -1 : 1;
			poseStack.translate((double)((float)i * this.translation.x()), (double)this.translation.y(), (double)this.translation.z());
			poseStack.mulPose(new Quaternion(f, g, h, true));
			poseStack.scale(this.scale.x(), this.scale.y(), this.scale.z());
		}
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (this.getClass() != object.getClass()) {
			return false;
		} else {
			ItemTransform itemTransform = (ItemTransform)object;
			return this.rotation.equals(itemTransform.rotation) && this.scale.equals(itemTransform.scale) && this.translation.equals(itemTransform.translation);
		}
	}

	public int hashCode() {
		int i = this.rotation.hashCode();
		i = 31 * i + this.translation.hashCode();
		return 31 * i + this.scale.hashCode();
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<ItemTransform> {
		private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
		private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
		private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);

		protected Deserializer() {
		}

		public ItemTransform deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Vector3f vector3f = this.getVector3f(jsonObject, "rotation", DEFAULT_ROTATION);
			Vector3f vector3f2 = this.getVector3f(jsonObject, "translation", DEFAULT_TRANSLATION);
			vector3f2.mul(0.0625F);
			vector3f2.clamp(-5.0F, 5.0F);
			Vector3f vector3f3 = this.getVector3f(jsonObject, "scale", DEFAULT_SCALE);
			vector3f3.clamp(-4.0F, 4.0F);
			return new ItemTransform(vector3f, vector3f2, vector3f3);
		}

		private Vector3f getVector3f(JsonObject jsonObject, String string, Vector3f vector3f) {
			if (!jsonObject.has(string)) {
				return vector3f;
			} else {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, string);
				if (jsonArray.size() != 3) {
					throw new JsonParseException("Expected 3 " + string + " values, found: " + jsonArray.size());
				} else {
					float[] fs = new float[3];

					for (int i = 0; i < fs.length; i++) {
						fs[i] = GsonHelper.convertToFloat(jsonArray.get(i), string + "[" + i + "]");
					}

					return new Vector3f(fs[0], fs[1], fs[2]);
				}
			}
		}
	}
}
