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
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		return new StartRidingTrigger.TriggerInstance(composite);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(EntityPredicate.Composite composite) {
			super(StartRidingTrigger.ID, composite);
		}

		public static StartRidingTrigger.TriggerInstance playerStartsRiding(EntityPredicate.Builder builder) {
			return new StartRidingTrigger.TriggerInstance(EntityPredicate.Composite.wrap(builder.build()));
		}
	}
}
