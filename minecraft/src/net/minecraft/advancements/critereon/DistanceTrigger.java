package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("start_position"));
		DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
		return new DistanceTrigger.TriggerInstance(this.id, contextAwarePredicate, locationPredicate, distancePredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
		Vec3 vec32 = serverPlayer.position();
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final LocationPredicate startPosition;
		private final DistancePredicate distance;

		public TriggerInstance(
			ResourceLocation resourceLocation, ContextAwarePredicate contextAwarePredicate, LocationPredicate locationPredicate, DistancePredicate distancePredicate
		) {
			super(resourceLocation, contextAwarePredicate);
			this.startPosition = locationPredicate;
			this.distance = distancePredicate;
		}

		public static DistanceTrigger.TriggerInstance fallFromHeight(
			EntityPredicate.Builder builder, DistancePredicate distancePredicate, LocationPredicate locationPredicate
		) {
			return new DistanceTrigger.TriggerInstance(CriteriaTriggers.FALL_FROM_HEIGHT.id, EntityPredicate.wrap(builder.build()), locationPredicate, distancePredicate);
		}

		public static DistanceTrigger.TriggerInstance rideEntityInLava(EntityPredicate.Builder builder, DistancePredicate distancePredicate) {
			return new DistanceTrigger.TriggerInstance(
				CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.id, EntityPredicate.wrap(builder.build()), LocationPredicate.ANY, distancePredicate
			);
		}

		public static DistanceTrigger.TriggerInstance travelledThroughNether(DistancePredicate distancePredicate) {
			return new DistanceTrigger.TriggerInstance(CriteriaTriggers.NETHER_TRAVEL.id, ContextAwarePredicate.ANY, LocationPredicate.ANY, distancePredicate);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("start_position", this.startPosition.serializeToJson());
			jsonObject.add("distance", this.distance.serializeToJson());
			return jsonObject;
		}

		public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
			return !this.startPosition.matches(serverLevel, vec3.x, vec3.y, vec3.z) ? false : this.distance.matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
		}
	}
}
