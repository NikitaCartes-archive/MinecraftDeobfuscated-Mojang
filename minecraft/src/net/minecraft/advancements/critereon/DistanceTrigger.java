package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
	public DistanceTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<LocationPredicate> optional2 = LocationPredicate.fromJson(jsonObject.get("start_position"));
		Optional<DistancePredicate> optional3 = DistancePredicate.fromJson(jsonObject.get("distance"));
		return new DistanceTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
		Vec3 vec32 = serverPlayer.position();
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<LocationPredicate> startPosition;
		private final Optional<DistancePredicate> distance;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<LocationPredicate> optional2, Optional<DistancePredicate> optional3) {
			super(optional);
			this.startPosition = optional2;
			this.distance = optional3;
		}

		public static Criterion<DistanceTrigger.TriggerInstance> fallFromHeight(
			EntityPredicate.Builder builder, DistancePredicate distancePredicate, LocationPredicate.Builder builder2
		) {
			return CriteriaTriggers.FALL_FROM_HEIGHT
				.createCriterion(
					new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build()), Optional.of(distancePredicate))
				);
		}

		public static Criterion<DistanceTrigger.TriggerInstance> rideEntityInLava(EntityPredicate.Builder builder, DistancePredicate distancePredicate) {
			return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER
				.createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.empty(), Optional.of(distancePredicate)));
		}

		public static Criterion<DistanceTrigger.TriggerInstance> travelledThroughNether(DistancePredicate distancePredicate) {
			return CriteriaTriggers.NETHER_TRAVEL
				.createCriterion(new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distancePredicate)));
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.startPosition.ifPresent(locationPredicate -> jsonObject.add("start_position", locationPredicate.serializeToJson()));
			this.distance.ifPresent(distancePredicate -> jsonObject.add("distance", distancePredicate.serializeToJson()));
			return jsonObject;
		}

		public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
			return this.startPosition.isPresent() && !((LocationPredicate)this.startPosition.get()).matches(serverLevel, vec3.x, vec3.y, vec3.z)
				? false
				: !this.distance.isPresent() || ((DistancePredicate)this.distance.get()).matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
		}
	}
}
