package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
	public ConstructBeaconTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
		return new ConstructBeaconTrigger.TriggerInstance(optional, ints);
	}

	public void trigger(ServerPlayer serverPlayer, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints level;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, MinMaxBounds.Ints ints) {
			super(optional);
			this.level = ints;
		}

		public static Criterion<ConstructBeaconTrigger.TriggerInstance> constructedBeacon() {
			return CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new ConstructBeaconTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY));
		}

		public static Criterion<ConstructBeaconTrigger.TriggerInstance> constructedBeacon(MinMaxBounds.Ints ints) {
			return CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new ConstructBeaconTrigger.TriggerInstance(Optional.empty(), ints));
		}

		public boolean matches(int i) {
			return this.level.matches(i);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			jsonObject.add("level", this.level.serializeToJson());
			return jsonObject;
		}
	}
}
