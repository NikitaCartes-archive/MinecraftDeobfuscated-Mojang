package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ItemTransforms {
	public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms();
	public static float transX;
	public static float transY;
	public static float transZ;
	public static float rotX;
	public static float rotY;
	public static float rotZ;
	public static float scaleX;
	public static float scaleY;
	public static float scaleZ;
	public final ItemTransform thirdPersonLeftHand;
	public final ItemTransform thirdPersonRightHand;
	public final ItemTransform firstPersonLeftHand;
	public final ItemTransform firstPersonRightHand;
	public final ItemTransform head;
	public final ItemTransform gui;
	public final ItemTransform ground;
	public final ItemTransform fixed;

	private ItemTransforms() {
		this(
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM
		);
	}

	public ItemTransforms(ItemTransforms itemTransforms) {
		this.thirdPersonLeftHand = itemTransforms.thirdPersonLeftHand;
		this.thirdPersonRightHand = itemTransforms.thirdPersonRightHand;
		this.firstPersonLeftHand = itemTransforms.firstPersonLeftHand;
		this.firstPersonRightHand = itemTransforms.firstPersonRightHand;
		this.head = itemTransforms.head;
		this.gui = itemTransforms.gui;
		this.ground = itemTransforms.ground;
		this.fixed = itemTransforms.fixed;
	}

	public ItemTransforms(
		ItemTransform itemTransform,
		ItemTransform itemTransform2,
		ItemTransform itemTransform3,
		ItemTransform itemTransform4,
		ItemTransform itemTransform5,
		ItemTransform itemTransform6,
		ItemTransform itemTransform7,
		ItemTransform itemTransform8
	) {
		this.thirdPersonLeftHand = itemTransform;
		this.thirdPersonRightHand = itemTransform2;
		this.firstPersonLeftHand = itemTransform3;
		this.firstPersonRightHand = itemTransform4;
		this.head = itemTransform5;
		this.gui = itemTransform6;
		this.ground = itemTransform7;
		this.fixed = itemTransform8;
	}

	public void apply(ItemTransforms.TransformType transformType) {
		apply(this.getTransform(transformType), false);
	}

	public static void apply(ItemTransform itemTransform, boolean bl) {
		if (itemTransform != ItemTransform.NO_TRANSFORM) {
			int i = bl ? -1 : 1;
			RenderSystem.translatef((float)i * (transX + itemTransform.translation.x()), transY + itemTransform.translation.y(), transZ + itemTransform.translation.z());
			float f = rotX + itemTransform.rotation.x();
			float g = rotY + itemTransform.rotation.y();
			float h = rotZ + itemTransform.rotation.z();
			if (bl) {
				g = -g;
				h = -h;
			}

			RenderSystem.multMatrix(new Matrix4f(new Quaternion(f, g, h, true)));
			RenderSystem.scalef(scaleX + itemTransform.scale.x(), scaleY + itemTransform.scale.y(), scaleZ + itemTransform.scale.z());
		}
	}

	public ItemTransform getTransform(ItemTransforms.TransformType transformType) {
		switch (transformType) {
			case THIRD_PERSON_LEFT_HAND:
				return this.thirdPersonLeftHand;
			case THIRD_PERSON_RIGHT_HAND:
				return this.thirdPersonRightHand;
			case FIRST_PERSON_LEFT_HAND:
				return this.firstPersonLeftHand;
			case FIRST_PERSON_RIGHT_HAND:
				return this.firstPersonRightHand;
			case HEAD:
				return this.head;
			case GUI:
				return this.gui;
			case GROUND:
				return this.ground;
			case FIXED:
				return this.fixed;
			default:
				return ItemTransform.NO_TRANSFORM;
		}
	}

	public boolean hasTransform(ItemTransforms.TransformType transformType) {
		return this.getTransform(transformType) != ItemTransform.NO_TRANSFORM;
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<ItemTransforms> {
		protected Deserializer() {
		}

		public ItemTransforms deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			ItemTransform itemTransform = this.getTransform(jsonDeserializationContext, jsonObject, "thirdperson_righthand");
			ItemTransform itemTransform2 = this.getTransform(jsonDeserializationContext, jsonObject, "thirdperson_lefthand");
			if (itemTransform2 == ItemTransform.NO_TRANSFORM) {
				itemTransform2 = itemTransform;
			}

			ItemTransform itemTransform3 = this.getTransform(jsonDeserializationContext, jsonObject, "firstperson_righthand");
			ItemTransform itemTransform4 = this.getTransform(jsonDeserializationContext, jsonObject, "firstperson_lefthand");
			if (itemTransform4 == ItemTransform.NO_TRANSFORM) {
				itemTransform4 = itemTransform3;
			}

			ItemTransform itemTransform5 = this.getTransform(jsonDeserializationContext, jsonObject, "head");
			ItemTransform itemTransform6 = this.getTransform(jsonDeserializationContext, jsonObject, "gui");
			ItemTransform itemTransform7 = this.getTransform(jsonDeserializationContext, jsonObject, "ground");
			ItemTransform itemTransform8 = this.getTransform(jsonDeserializationContext, jsonObject, "fixed");
			return new ItemTransforms(itemTransform2, itemTransform, itemTransform4, itemTransform3, itemTransform5, itemTransform6, itemTransform7, itemTransform8);
		}

		private ItemTransform getTransform(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject, String string) {
			return jsonObject.has(string) ? jsonDeserializationContext.deserialize(jsonObject.get(string), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum TransformType {
		NONE,
		THIRD_PERSON_LEFT_HAND,
		THIRD_PERSON_RIGHT_HAND,
		FIRST_PERSON_LEFT_HAND,
		FIRST_PERSON_RIGHT_HAND,
		HEAD,
		GUI,
		GROUND,
		FIXED;
	}
}
