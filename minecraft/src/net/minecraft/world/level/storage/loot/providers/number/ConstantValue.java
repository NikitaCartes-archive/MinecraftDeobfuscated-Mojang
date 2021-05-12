package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;

public final class ConstantValue implements NumberProvider {
	final float value;

	ConstantValue(float f) {
		this.value = f;
	}

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.CONSTANT;
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return this.value;
	}

	public static ConstantValue exactly(float f) {
		return new ConstantValue(f);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object != null && this.getClass() == object.getClass() ? Float.compare(((ConstantValue)object).value, this.value) == 0 : false;
		}
	}

	public int hashCode() {
		return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
	}

	public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ConstantValue> {
		public JsonElement serialize(ConstantValue constantValue, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(constantValue.value);
		}

		public ConstantValue deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
			return new ConstantValue(GsonHelper.convertToFloat(jsonElement, "value"));
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConstantValue> {
		public void serialize(JsonObject jsonObject, ConstantValue constantValue, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("value", constantValue.value);
		}

		public ConstantValue deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			float f = GsonHelper.getAsFloat(jsonObject, "value");
			return new ConstantValue(f);
		}
	}
}
