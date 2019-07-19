package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class RandomIntGenerators {
	private static final Map<ResourceLocation, Class<? extends RandomIntGenerator>> GENERATORS = Maps.<ResourceLocation, Class<? extends RandomIntGenerator>>newHashMap();

	public static RandomIntGenerator deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		if (jsonElement.isJsonPrimitive()) {
			return jsonDeserializationContext.deserialize(jsonElement, ConstantIntValue.class);
		} else {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			String string = GsonHelper.getAsString(jsonObject, "type", RandomIntGenerator.UNIFORM.toString());
			Class<? extends RandomIntGenerator> class_ = (Class<? extends RandomIntGenerator>)GENERATORS.get(new ResourceLocation(string));
			if (class_ == null) {
				throw new JsonParseException("Unknown generator: " + string);
			} else {
				return jsonDeserializationContext.deserialize(jsonObject, class_);
			}
		}
	}

	public static JsonElement serialize(RandomIntGenerator randomIntGenerator, JsonSerializationContext jsonSerializationContext) {
		JsonElement jsonElement = jsonSerializationContext.serialize(randomIntGenerator);
		if (jsonElement.isJsonObject()) {
			jsonElement.getAsJsonObject().addProperty("type", randomIntGenerator.getType().toString());
		}

		return jsonElement;
	}

	static {
		GENERATORS.put(RandomIntGenerator.UNIFORM, RandomValueBounds.class);
		GENERATORS.put(RandomIntGenerator.BINOMIAL, BinomialDistributionGenerator.class);
		GENERATORS.put(RandomIntGenerator.CONSTANT, ConstantIntValue.class);
	}
}
