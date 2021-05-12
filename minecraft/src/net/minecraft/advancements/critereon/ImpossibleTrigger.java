package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger implements CriterionTrigger<ImpossibleTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("impossible");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> listener) {
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> listener) {
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
	}

	public ImpossibleTrigger.TriggerInstance createInstance(JsonObject jsonObject, DeserializationContext deserializationContext) {
		return new ImpossibleTrigger.TriggerInstance();
	}

	public static class TriggerInstance implements CriterionTriggerInstance {
		@Override
		public ResourceLocation getCriterion() {
			return ImpossibleTrigger.ID;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			return new JsonObject();
		}
	}
}
