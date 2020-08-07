package net.minecraft.data.models.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class DelegatedModel implements Supplier<JsonElement> {
	private final ResourceLocation parent;

	public DelegatedModel(ResourceLocation resourceLocation) {
		this.parent = resourceLocation;
	}

	public JsonElement get() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("parent", this.parent.toString());
		return jsonObject;
	}
}
