package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class ItemTransforms {
	public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms();
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

	public ItemTransform getTransform(ItemDisplayContext itemDisplayContext) {
		return switch (itemDisplayContext) {
			case THIRD_PERSON_LEFT_HAND -> this.thirdPersonLeftHand;
			case THIRD_PERSON_RIGHT_HAND -> this.thirdPersonRightHand;
			case FIRST_PERSON_LEFT_HAND -> this.firstPersonLeftHand;
			case FIRST_PERSON_RIGHT_HAND -> this.firstPersonRightHand;
			case HEAD -> this.head;
			case GUI -> this.gui;
			case GROUND -> this.ground;
			case FIXED -> this.fixed;
			default -> ItemTransform.NO_TRANSFORM;
		};
	}

	public boolean hasTransform(ItemDisplayContext itemDisplayContext) {
		return this.getTransform(itemDisplayContext) != ItemTransform.NO_TRANSFORM;
	}

	@Environment(EnvType.CLIENT)
	protected static class Deserializer implements JsonDeserializer<ItemTransforms> {
		public ItemTransforms deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			ItemTransform itemTransform = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
			ItemTransform itemTransform2 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
			if (itemTransform2 == ItemTransform.NO_TRANSFORM) {
				itemTransform2 = itemTransform;
			}

			ItemTransform itemTransform3 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
			ItemTransform itemTransform4 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
			if (itemTransform4 == ItemTransform.NO_TRANSFORM) {
				itemTransform4 = itemTransform3;
			}

			ItemTransform itemTransform5 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.HEAD);
			ItemTransform itemTransform6 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.GUI);
			ItemTransform itemTransform7 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.GROUND);
			ItemTransform itemTransform8 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.FIXED);
			return new ItemTransforms(itemTransform2, itemTransform, itemTransform4, itemTransform3, itemTransform5, itemTransform6, itemTransform7, itemTransform8);
		}

		private ItemTransform getTransform(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject, ItemDisplayContext itemDisplayContext) {
			String string = itemDisplayContext.getSerializedName();
			return jsonObject.has(string) ? jsonDeserializationContext.deserialize(jsonObject.get(string), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
		}
	}
}
