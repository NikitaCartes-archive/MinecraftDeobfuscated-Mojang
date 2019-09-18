package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
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

	public LevitationTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
		return new LevitationTrigger.TriggerInstance(distancePredicate, ints);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, vec3, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DistancePredicate distance;
		private final MinMaxBounds.Ints duration;

		public TriggerInstance(DistancePredicate distancePredicate, MinMaxBounds.Ints ints) {
			super(LevitationTrigger.ID);
			this.distance = distancePredicate;
			this.duration = ints;
		}

		public static LevitationTrigger.TriggerInstance levitated(DistancePredicate distancePredicate) {
			return new LevitationTrigger.TriggerInstance(distancePredicate, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
			return !this.distance.matches(vec3.x, vec3.y, vec3.z, serverPlayer.x, serverPlayer.y, serverPlayer.z) ? false : this.duration.matches(i);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("distance", this.distance.serializeToJson());
			jsonObject.add("duration", this.duration.serializeToJson());
			return jsonObject;
		}
	}
}
