package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.minecraft.resources.ResourceLocation;

public interface CriterionTriggerInstance {
	ResourceLocation getCriterion();

	default JsonElement serializeToJson() {
		return JsonNull.INSTANCE;
	}
}
