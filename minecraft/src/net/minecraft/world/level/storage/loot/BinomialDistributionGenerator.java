package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class BinomialDistributionGenerator implements RandomIntGenerator {
	private final int n;
	private final float p;

	public BinomialDistributionGenerator(int i, float f) {
		this.n = i;
		this.p = f;
	}

	@Override
	public int getInt(Random random) {
		int i = 0;

		for (int j = 0; j < this.n; j++) {
			if (random.nextFloat() < this.p) {
				i++;
			}
		}

		return i;
	}

	public static BinomialDistributionGenerator binomial(int i, float f) {
		return new BinomialDistributionGenerator(i, f);
	}

	@Override
	public ResourceLocation getType() {
		return BINOMIAL;
	}

	public static class Serializer implements JsonDeserializer<BinomialDistributionGenerator>, JsonSerializer<BinomialDistributionGenerator> {
		public BinomialDistributionGenerator deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
			int i = GsonHelper.getAsInt(jsonObject, "n");
			float f = GsonHelper.getAsFloat(jsonObject, "p");
			return new BinomialDistributionGenerator(i, f);
		}

		public JsonElement serialize(BinomialDistributionGenerator binomialDistributionGenerator, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("n", binomialDistributionGenerator.n);
			jsonObject.addProperty("p", binomialDistributionGenerator.p);
			return jsonObject;
		}
	}
}
