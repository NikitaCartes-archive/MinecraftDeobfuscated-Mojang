package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("started_riding");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public StartRidingTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		return new StartRidingTrigger.TriggerInstance(contextAwarePredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(ContextAwarePredicate contextAwarePredicate) {
			super(StartRidingTrigger.ID, contextAwarePredicate);
		}

		public static StartRidingTrigger.TriggerInstance playerStartsRiding(EntityPredicate.Builder builder) {
			return new StartRidingTrigger.TriggerInstance(EntityPredicate.wrap(builder.build()));
		}
	}
}
