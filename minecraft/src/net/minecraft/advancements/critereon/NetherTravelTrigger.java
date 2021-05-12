package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetherTravelTrigger extends SimpleCriterionTrigger<NetherTravelTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("nether_travel");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public NetherTravelTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("entered"));
		LocationPredicate locationPredicate2 = LocationPredicate.fromJson(jsonObject.get("exited"));
		DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
		return new NetherTravelTrigger.TriggerInstance(composite, locationPredicate, locationPredicate2, distancePredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
		this.trigger(
			serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.getLevel(), vec3, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ())
		);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final LocationPredicate entered;
		private final LocationPredicate exited;
		private final DistancePredicate distance;

		public TriggerInstance(
			EntityPredicate.Composite composite, LocationPredicate locationPredicate, LocationPredicate locationPredicate2, DistancePredicate distancePredicate
		) {
			super(NetherTravelTrigger.ID, composite);
			this.entered = locationPredicate;
			this.exited = locationPredicate2;
			this.distance = distancePredicate;
		}

		public static NetherTravelTrigger.TriggerInstance travelledThroughNether(DistancePredicate distancePredicate) {
			return new NetherTravelTrigger.TriggerInstance(EntityPredicate.Composite.ANY, LocationPredicate.ANY, LocationPredicate.ANY, distancePredicate);
		}

		public boolean matches(ServerLevel serverLevel, Vec3 vec3, double d, double e, double f) {
			if (!this.entered.matches(serverLevel, vec3.x, vec3.y, vec3.z)) {
				return false;
			} else {
				return !this.exited.matches(serverLevel, d, e, f) ? false : this.distance.matches(vec3.x, vec3.y, vec3.z, d, e, f);
			}
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("entered", this.entered.serializeToJson());
			jsonObject.add("exited", this.exited.serializeToJson());
			jsonObject.add("distance", this.distance.serializeToJson());
			return jsonObject;
		}
	}
}
