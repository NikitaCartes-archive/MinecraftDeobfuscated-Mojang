package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("started_riding");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public StartRidingTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		return new StartRidingTrigger.TriggerInstance(optional);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(Optional<ContextAwarePredicate> optional) {
			super(StartRidingTrigger.ID, optional);
		}

		public static StartRidingTrigger.TriggerInstance playerStartsRiding(EntityPredicate.Builder builder) {
			return new StartRidingTrigger.TriggerInstance(EntityPredicate.wrap(builder));
		}
	}
}
