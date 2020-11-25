package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemOverride {
	private final ResourceLocation model;
	private final Map<ResourceLocation, Float> predicates;

	public ItemOverride(ResourceLocation resourceLocation, Map<ResourceLocation, Float> map) {
		this.model = resourceLocation;
		this.predicates = map;
	}

	public ResourceLocation getModel() {
		return this.model;
	}

	boolean test(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
		Item item = itemStack.getItem();

		for (Entry<ResourceLocation, Float> entry : this.predicates.entrySet()) {
			ItemPropertyFunction itemPropertyFunction = ItemProperties.getProperty(item, (ResourceLocation)entry.getKey());
			if (itemPropertyFunction == null || itemPropertyFunction.call(itemStack, clientLevel, livingEntity, i) < (Float)entry.getValue()) {
				return false;
			}
		}

		return true;
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<ItemOverride> {
		protected Deserializer() {
		}

		public ItemOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "model"));
			Map<ResourceLocation, Float> map = this.getPredicates(jsonObject);
			return new ItemOverride(resourceLocation, map);
		}

		protected Map<ResourceLocation, Float> getPredicates(JsonObject jsonObject) {
			Map<ResourceLocation, Float> map = Maps.<ResourceLocation, Float>newLinkedHashMap();
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "predicate");

			for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
				map.put(new ResourceLocation((String)entry.getKey()), GsonHelper.convertToFloat((JsonElement)entry.getValue(), (String)entry.getKey()));
			}

			return map;
		}
	}
}
