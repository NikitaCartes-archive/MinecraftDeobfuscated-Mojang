package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Criterion {
	@Nullable
	private final CriterionTriggerInstance trigger;

	public Criterion(CriterionTriggerInstance criterionTriggerInstance) {
		this.trigger = criterionTriggerInstance;
	}

	public Criterion() {
		this.trigger = null;
	}

	public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
	}

	public static Criterion criterionFromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "trigger"));
		CriterionTrigger<?> criterionTrigger = CriteriaTriggers.getCriterion(resourceLocation);
		if (criterionTrigger == null) {
			throw new JsonSyntaxException("Invalid criterion trigger: " + resourceLocation);
		} else {
			CriterionTriggerInstance criterionTriggerInstance = criterionTrigger.createInstance(
				GsonHelper.getAsJsonObject(jsonObject, "conditions", new JsonObject()), deserializationContext
			);
			return new Criterion(criterionTriggerInstance);
		}
	}

	public static Criterion criterionFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		return new Criterion();
	}

	public static Map<String, Criterion> criteriaFromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
		Map<String, Criterion> map = Maps.<String, Criterion>newHashMap();

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			map.put((String)entry.getKey(), criterionFromJson(GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), "criterion"), deserializationContext));
		}

		return map;
	}

	public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		return friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, Criterion::criterionFromNetwork);
	}

	public static void serializeToNetwork(Map<String, Criterion> map, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeMap(map, FriendlyByteBuf::writeUtf, (friendlyByteBufx, criterion) -> criterion.serializeToNetwork(friendlyByteBufx));
	}

	@Nullable
	public CriterionTriggerInstance getTrigger() {
		return this.trigger;
	}

	public JsonElement serializeToJson() {
		if (this.trigger == null) {
			throw new JsonSyntaxException("Missing trigger");
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("trigger", this.trigger.getCriterion().toString());
			JsonObject jsonObject2 = this.trigger.serializeToJson();
			if (jsonObject2.size() != 0) {
				jsonObject.add("conditions", jsonObject2);
			}

			return jsonObject;
		}
	}
}
