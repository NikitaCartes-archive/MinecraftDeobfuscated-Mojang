package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public record ItemOverride(ResourceLocation model, List<ItemOverride.Predicate> predicates) {
	public ItemOverride(ResourceLocation model, List<ItemOverride.Predicate> predicates) {
		predicates = List.copyOf(predicates);
		this.model = model;
		this.predicates = predicates;
	}

	@Environment(EnvType.CLIENT)
	protected static class Deserializer implements JsonDeserializer<ItemOverride> {
		public ItemOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			ResourceLocation resourceLocation = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "model"));
			List<ItemOverride.Predicate> list = this.getPredicates(jsonObject);
			return new ItemOverride(resourceLocation, list);
		}

		protected List<ItemOverride.Predicate> getPredicates(JsonObject jsonObject) {
			Map<ResourceLocation, Float> map = Maps.<ResourceLocation, Float>newLinkedHashMap();
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "predicate");

			for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
				map.put(ResourceLocation.parse((String)entry.getKey()), GsonHelper.convertToFloat((JsonElement)entry.getValue(), (String)entry.getKey()));
			}

			return (List<ItemOverride.Predicate>)map.entrySet()
				.stream()
				.map(entryx -> new ItemOverride.Predicate((ResourceLocation)entryx.getKey(), (Float)entryx.getValue()))
				.collect(ImmutableList.toImmutableList());
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Predicate(ResourceLocation property, float value) {
	}
}
