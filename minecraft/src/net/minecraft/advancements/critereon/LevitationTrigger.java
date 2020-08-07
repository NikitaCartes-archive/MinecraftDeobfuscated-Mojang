package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("levitation");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public LevitationTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
		return new LevitationTrigger.TriggerInstance(composite, distancePredicate, ints);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, vec3, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DistancePredicate distance;
		private final MinMaxBounds.Ints duration;

		public TriggerInstance(EntityPredicate.Composite composite, DistancePredicate distancePredicate, MinMaxBounds.Ints ints) {
			super(LevitationTrigger.ID, composite);
			this.distance = distancePredicate;
			this.duration = ints;
		}

		public static LevitationTrigger.TriggerInstance levitated(DistancePredicate distancePredicate) {
			return new LevitationTrigger.TriggerInstance(EntityPredicate.Composite.ANY, distancePredicate, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
			return !this.distance.matches(vec3.x, vec3.y, vec3.z, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()) ? false : this.duration.matches(i);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("distance", this.distance.serializeToJson());
			jsonObject.add("duration", this.duration.serializeToJson());
			return jsonObject;
		}
	}
}
