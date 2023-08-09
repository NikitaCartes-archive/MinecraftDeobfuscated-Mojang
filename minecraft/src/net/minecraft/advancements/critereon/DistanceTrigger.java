package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
	final ResourceLocation id;

	public DistanceTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public DistanceTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<LocationPredicate> optional2 = LocationPredicate.fromJson(jsonObject.get("start_position"));
		Optional<DistancePredicate> optional3 = DistancePredicate.fromJson(jsonObject.get("distance"));
		return new DistanceTrigger.TriggerInstance(this.id, optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
		Vec3 vec32 = serverPlayer.position();
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<LocationPredicate> startPosition;
		private final Optional<DistancePredicate> distance;

		public TriggerInstance(
			ResourceLocation resourceLocation, Optional<ContextAwarePredicate> optional, Optional<LocationPredicate> optional2, Optional<DistancePredicate> optional3
		) {
			super(resourceLocation, optional);
			this.startPosition = optional2;
			this.distance = optional3;
		}

		public static DistanceTrigger.TriggerInstance fallFromHeight(
			EntityPredicate.Builder builder, DistancePredicate distancePredicate, LocationPredicate.Builder builder2
		) {
			return new DistanceTrigger.TriggerInstance(
				CriteriaTriggers.FALL_FROM_HEIGHT.id, EntityPredicate.wrap(builder), builder2.build(), Optional.of(distancePredicate)
			);
		}

		public static DistanceTrigger.TriggerInstance rideEntityInLava(EntityPredicate.Builder builder, DistancePredicate distancePredicate) {
			return new DistanceTrigger.TriggerInstance(
				CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.id, EntityPredicate.wrap(builder), Optional.empty(), Optional.of(distancePredicate)
			);
		}

		public static DistanceTrigger.TriggerInstance travelledThroughNether(DistancePredicate distancePredicate) {
			return new DistanceTrigger.TriggerInstance(CriteriaTriggers.NETHER_TRAVEL.id, Optional.empty(), Optional.empty(), Optional.of(distancePredicate));
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
