package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
	public LevitationTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<DistancePredicate> optional2 = DistancePredicate.fromJson(jsonObject.get("distance"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
		return new LevitationTrigger.TriggerInstance(optional, optional2, ints);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, vec3, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<DistancePredicate> distance;
		private final MinMaxBounds.Ints duration;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<DistancePredicate> optional2, MinMaxBounds.Ints ints) {
			super(optional);
			this.distance = optional2;
			this.duration = ints;
		}

		public static Criterion<LevitationTrigger.TriggerInstance> levitated(DistancePredicate distancePredicate) {
			return CriteriaTriggers.LEVITATION
				.createCriterion(new LevitationTrigger.TriggerInstance(Optional.empty(), Optional.of(distancePredicate), MinMaxBounds.Ints.ANY));
		}

		public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
			return this.distance.isPresent()
					&& !((DistancePredicate)this.distance.get()).matches(vec3.x, vec3.y, vec3.z, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ())
				? false
				: this.duration.matches(i);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.distance.ifPresent(distancePredicate -> jsonObject.add("distance", distancePredicate.serializeToJson()));
			jsonObject.add("duration", this.duration.serializeToJson());
			return jsonObject;
		}
	}
}
