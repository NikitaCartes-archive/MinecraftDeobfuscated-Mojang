package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TickTrigger extends SimpleCriterionTrigger<TickTrigger.TriggerInstance> {
	public static final ResourceLocation ID = new ResourceLocation("tick");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TickTrigger.TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
		return new TickTrigger.TriggerInstance(composite);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(EntityPredicate.Composite composite) {
			super(TickTrigger.ID, composite);
		}
	}
}
