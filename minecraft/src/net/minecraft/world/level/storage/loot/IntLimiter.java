package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class IntLimiter implements IntUnaryOperator {
	private final Integer min;
	private final Integer max;
	private final IntUnaryOperator op;

	private IntLimiter(@Nullable Integer integer, @Nullable Integer integer2) {
		this.min = integer;
		this.max = integer2;
		if (integer == null) {
			if (integer2 == null) {
				this.op = ix -> ix;
			} else {
				int i = integer2;
				this.op = jx -> Math.min(i, jx);
			}
		} else {
			int i = integer;
			if (integer2 == null) {
				this.op = jx -> Math.max(i, jx);
			} else {
				int j = integer2;
				this.op = k -> Mth.clamp(k, i, j);
			}
		}
	}

	public static IntLimiter clamp(int i, int j) {
		return new IntLimiter(i, j);
	}

	public static IntLimiter lowerBound(int i) {
		return new IntLimiter(i, null);
	}

	public static IntLimiter upperBound(int i) {
		return new IntLimiter(null, i);
	}

	public int applyAsInt(int i) {
		return this.op.applyAsInt(i);
	}

	public static class Serializer implements JsonDeserializer<IntLimiter>, JsonSerializer<IntLimiter> {
		public IntLimiter deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
			Integer integer = jsonObject.has("min") ? GsonHelper.getAsInt(jsonObject, "min") : null;
			Integer integer2 = jsonObject.has("max") ? GsonHelper.getAsInt(jsonObject, "max") : null;
			return new IntLimiter(integer, integer2);
		}

		public JsonElement serialize(IntLimiter intLimiter, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			if (intLimiter.max != null) {
				jsonObject.addProperty("max", intLimiter.max);
			}

			if (intLimiter.min != null) {
				jsonObject.addProperty("min", intLimiter.min);
			}

			return jsonObject;
		}
	}
}
