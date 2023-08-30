package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record Criterion<T extends CriterionTriggerInstance>(CriterionTrigger<T> trigger, T triggerInstance) {
	public static Criterion<?> criterionFromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "trigger"));
		CriterionTrigger<?> criterionTrigger = CriteriaTriggers.getCriterion(resourceLocation);
		if (criterionTrigger == null) {
			throw new JsonSyntaxException("Invalid criterion trigger: " + resourceLocation);
		} else {
			return criterionFromJson(jsonObject, deserializationContext, criterionTrigger);
		}
	}

	private static <T extends CriterionTriggerInstance> Criterion<T> criterionFromJson(
		JsonObject jsonObject, DeserializationContext deserializationContext, CriterionTrigger<T> criterionTrigger
	) {
		T criterionTriggerInstance = criterionTrigger.createInstance(GsonHelper.getAsJsonObject(jsonObject, "conditions", new JsonObject()), deserializationContext);
		return new Criterion<>(criterionTrigger, criterionTriggerInstance);
	}

	public static Map<String, Criterion<?>> criteriaFromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
		Map<String, Criterion<?>> map = Maps.<String, Criterion<?>>newHashMap();

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			map.put((String)entry.getKey(), criterionFromJson(GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), "criterion"), deserializationContext));
		}

		return map;
	}

	public JsonElement serializeToJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("trigger", ((ResourceLocation)Objects.requireNonNull(CriteriaTriggers.getId(this.trigger), "Unregistered trigger")).toString());
		JsonObject jsonObject2 = this.triggerInstance.serializeToJson();
		if (jsonObject2.size() != 0) {
			jsonObject.add("conditions", jsonObject2);
		}

		return jsonObject;
	}
}
