package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.lang.reflect.Type;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class Variant implements ModelState {
	private final ResourceLocation modelLocation;
	private final Transformation rotation;
	private final boolean uvLock;
	private final int weight;

	public Variant(ResourceLocation resourceLocation, Transformation transformation, boolean bl, int i) {
		this.modelLocation = resourceLocation;
		this.rotation = transformation;
		this.uvLock = bl;
		this.weight = i;
	}

	public ResourceLocation getModelLocation() {
		return this.modelLocation;
	}

	@Override
	public Transformation getRotation() {
		return this.rotation;
	}

	@Override
	public boolean isUvLocked() {
		return this.uvLock;
	}

	public int getWeight() {
		return this.weight;
	}

	public String toString() {
		return "Variant{modelLocation=" + this.modelLocation + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof Variant variant)
				? false
				: this.modelLocation.equals(variant.modelLocation)
					&& Objects.equals(this.rotation, variant.rotation)
					&& this.uvLock == variant.uvLock
					&& this.weight == variant.weight;
		}
	}

	public int hashCode() {
		int i = this.modelLocation.hashCode();
		i = 31 * i + this.rotation.hashCode();
		i = 31 * i + Boolean.valueOf(this.uvLock).hashCode();
		return 31 * i + this.weight;
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<Variant> {
		@VisibleForTesting
		static final boolean DEFAULT_UVLOCK = false;
		@VisibleForTesting
		static final int DEFAULT_WEIGHT = 1;
		@VisibleForTesting
		static final int DEFAULT_X_ROTATION = 0;
		@VisibleForTesting
		static final int DEFAULT_Y_ROTATION = 0;

		public Variant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			ResourceLocation resourceLocation = this.getModel(jsonObject);
			BlockModelRotation blockModelRotation = this.getBlockRotation(jsonObject);
			boolean bl = this.getUvLock(jsonObject);
			int i = this.getWeight(jsonObject);
			return new Variant(resourceLocation, blockModelRotation.getRotation(), bl, i);
		}

		private boolean getUvLock(JsonObject jsonObject) {
			return GsonHelper.getAsBoolean(jsonObject, "uvlock", false);
		}

		protected BlockModelRotation getBlockRotation(JsonObject jsonObject) {
			int i = GsonHelper.getAsInt(jsonObject, "x", 0);
			int j = GsonHelper.getAsInt(jsonObject, "y", 0);
			BlockModelRotation blockModelRotation = BlockModelRotation.by(i, j);
			if (blockModelRotation == null) {
				throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
			} else {
				return blockModelRotation;
			}
		}

		protected ResourceLocation getModel(JsonObject jsonObject) {
			return ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "model"));
		}

		protected int getWeight(JsonObject jsonObject) {
			int i = GsonHelper.getAsInt(jsonObject, "weight", 1);
			if (i < 1) {
				throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
			} else {
				return i;
			}
		}
	}
}
