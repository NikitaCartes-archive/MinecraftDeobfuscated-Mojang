package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Variant implements Supplier<JsonElement> {
	private final Map<VariantProperty<?>, VariantProperty<?>.Value> values = Maps.<VariantProperty<?>, VariantProperty<?>.Value>newHashMap();

	public <T> Variant with(VariantProperty<T> variantProperty, T object) {
		VariantProperty<?>.Value value = (VariantProperty.Value)this.values.put(variantProperty, variantProperty.withValue(object));
		if (value != null) {
			throw new IllegalStateException("Replacing value of " + value + " with " + object);
		} else {
			return this;
		}
	}

	public static Variant variant() {
		return new Variant();
	}

	public static Variant merge(Variant variant, Variant variant2) {
		Variant variant3 = new Variant();
		variant3.values.putAll(variant.values);
		variant3.values.putAll(variant2.values);
		return variant3;
	}

	public JsonElement get() {
		JsonObject jsonObject = new JsonObject();
		this.values.values().forEach(value -> value.addToVariant(jsonObject));
		return jsonObject;
	}

	public static JsonElement convertList(List<Variant> list) {
		if (list.size() == 1) {
			return ((Variant)list.get(0)).get();
		} else {
			JsonArray jsonArray = new JsonArray();
			list.forEach(variant -> jsonArray.add(variant.get()));
			return jsonArray;
		}
	}
}
