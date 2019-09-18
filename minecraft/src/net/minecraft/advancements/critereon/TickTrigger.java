package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TickTrigger extends SimpleCriterionTrigger<TickTrigger.TriggerInstance> {
	public static final ResourceLocation ID = new ResourceLocation("tick");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TickTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new TickTrigger.TriggerInstance();
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer.getAdvancements());
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance() {
			super(TickTrigger.ID);
		}
	}
}
