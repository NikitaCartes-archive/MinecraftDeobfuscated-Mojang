package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class RandomValueBounds implements RandomIntGenerator {
	private final float min;
	private final float max;

	public RandomValueBounds(float f, float g) {
		this.min = f;
		this.max = g;
	}

	public RandomValueBounds(float f) {
		this.min = f;
		this.max = f;
	}

	public static RandomValueBounds between(float f, float g) {
		return new RandomValueBounds(f, g);
	}

	public float getMin() {
		return this.min;
	}

	public float getMax() {
		return this.max;
	}

	@Override
	public int getInt(Random random) {
		return Mth.nextInt(random, Mth.floor(this.min), Mth.floor(this.max));
	}

	public float getFloat(Random random) {
		return Mth.nextFloat(random, this.min, this.max);
	}

	public boolean matchesValue(int i) {
		return (float)i <= this.max && (float)i >= this.min;
	}

	@Override
	public ResourceLocation getType() {
		return UNIFORM;
	}

	public static class Serializer implements JsonDeserializer<RandomValueBounds>, JsonSerializer<RandomValueBounds> {
		public RandomValueBounds deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (GsonHelper.isNumberValue(jsonElement)) {
				return new RandomValueBounds(GsonHelper.convertToFloat(jsonElement, "value"));
			} else {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
				float f = GsonHelper.getAsFloat(jsonObject, "min");
				float g = GsonHelper.getAsFloat(jsonObject, "max");
				return new RandomValueBounds(f, g);
			}
		}

		public JsonElement serialize(RandomValueBounds randomValueBounds, Type type, JsonSerializationContext jsonSerializationContext) {
			if (randomValueBounds.min == randomValueBounds.max) {
				return new JsonPrimitive(randomValueBounds.min);
			} else {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("min", randomValueBounds.min);
				jsonObject.addProperty("max", randomValueBounds.max);
				return jsonObject;
			}
		}
	}
}
