package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
	public TargetBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("signal_strength"));
		Optional<ContextAwarePredicate> optional2 = EntityPredicate.fromJson(jsonObject, "projectile", deserializationContext);
		return new TargetBlockTrigger.TriggerInstance(optional, ints, optional2);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, Vec3 vec3, int i) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, vec3, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints signalStrength;
		private final Optional<ContextAwarePredicate> projectile;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, MinMaxBounds.Ints ints, Optional<ContextAwarePredicate> optional2) {
			super(optional);
			this.signalStrength = ints;
			this.projectile = optional2;
		}

		public static Criterion<TargetBlockTrigger.TriggerInstance> targetHit(MinMaxBounds.Ints ints, Optional<ContextAwarePredicate> optional) {
			return CriteriaTriggers.TARGET_BLOCK_HIT.createCriterion(new TargetBlockTrigger.TriggerInstance(Optional.empty(), ints, optional));
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			jsonObject.add("signal_strength", this.signalStrength.serializeToJson());
			this.projectile.ifPresent(contextAwarePredicate -> jsonObject.add("projectile", contextAwarePredicate.toJson()));
			return jsonObject;
		}

		public boolean matches(LootContext lootContext, Vec3 vec3, int i) {
			return !this.signalStrength.matches(i) ? false : !this.projectile.isPresent() || ((ContextAwarePredicate)this.projectile.get()).matches(lootContext);
		}
	}
}
