package net.minecraft.data.models.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;

public class VariantProperty<T> {
	private final String key;
	private final Function<T, JsonElement> serializer;

	public VariantProperty(String string, Function<T, JsonElement> function) {
		this.key = string;
		this.serializer = function;
	}

	public VariantProperty<T>.Value withValue(T object) {
		return new VariantProperty.Value(object);
	}

	public String toString() {
		return this.key;
	}

	public class Value {
		private final T value;

		public Value(T object) {
			this.value = object;
		}

		public void addToVariant(JsonObject jsonObject) {
			jsonObject.add(VariantProperty.this.key, (JsonElement)VariantProperty.this.serializer.apply(this.value));
		}

		public String toString() {
			return VariantProperty.this.key + "=" + this.value;
		}
	}
}