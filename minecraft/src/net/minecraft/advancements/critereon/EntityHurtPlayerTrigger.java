package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
	public EntityHurtPlayerTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<DamagePredicate> optional2 = DamagePredicate.fromJson(jsonObject.get("damage"));
		return new EntityHurtPlayerTrigger.TriggerInstance(optional, optional2);
	}

	public void trigger(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, damageSource, f, g, bl));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<DamagePredicate> damage;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<DamagePredicate> optional2) {
			super(optional);
			this.damage = optional2;
		}

		public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer() {
			return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
		}

		public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate damagePredicate) {
			return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(damagePredicate)));
		}

		public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate.Builder builder) {
			return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
		}

		public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
			return !this.damage.isPresent() || ((DamagePredicate)this.damage.get()).matches(serverPlayer, damageSource, f, g, bl);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.damage.ifPresent(damagePredicate -> jsonObject.add("damage", damagePredicate.serializeToJson()));
			return jsonObject;
		}
	}
}
