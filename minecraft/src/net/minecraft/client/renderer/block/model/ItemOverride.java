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
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class ItemOverride {
	private final ResourceLocation model;
	private final List<ItemOverride.Predicate> predicates;

	public ItemOverride(ResourceLocation resourceLocation, List<ItemOverride.Predicate> list) {
		this.model = resourceLocation;
		this.predicates = ImmutableList.copyOf(list);
	}

	public ResourceLocation getModel() {
		return this.model;
	}

	public Stream<ItemOverride.Predicate> getPredicates() {
		return this.predicates.stream();
	}

	@Environment(EnvType.CLIENT)
	protected static class Deserializer implements JsonDeserializer<ItemOverride> {
		public ItemOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "model"));
			List<ItemOverride.Predicate> list = this.getPredicates(jsonObject);
			return new ItemOverride(resourceLocation, list);
		}

		protected List<ItemOverride.Predicate> getPredicates(JsonObject jsonObject) {
			Map<ResourceLocation, Float> map = Maps.<ResourceLocation, Float>newLinkedHashMap();
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "predicate");

			for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
				map.put(new ResourceLocation((String)entry.getKey()), GsonHelper.convertToFloat((JsonElement)entry.getValue(), (String)entry.getKey()));
			}

			return (List<ItemOverride.Predicate>)map.entrySet()
				.stream()
				.map(entryx -> new ItemOverride.Predicate((ResourceLocation)entryx.getKey(), (Float)entryx.getValue()))
				.collect(ImmutableList.toImmutableList());
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Predicate {
		private final ResourceLocation property;
		private final float value;

		public Predicate(ResourceLocation resourceLocation, float f) {
			this.property = resourceLocation;
			this.value = f;
		}

		public ResourceLocation getProperty() {
			return this.property;
		}

		public float getValue() {
			return this.value;
		}
	}
}
