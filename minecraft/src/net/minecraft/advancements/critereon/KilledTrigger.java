package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
	public KilledTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		return new KilledTrigger.TriggerInstance(
			optional, EntityPredicate.fromJson(jsonObject, "entity", deserializationContext), DamageSourcePredicate.fromJson(jsonObject.get("killing_blow"))
		);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> entityPredicate;
		private final Optional<DamageSourcePredicate> killingBlow;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional2, Optional<DamageSourcePredicate> optional3) {
			super(optional);
			this.entityPredicate = optional2;
			this.killingBlow = optional3;
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity() {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), optional2));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), optional));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.of(builder.build())));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntityNearSculkCatalyst() {
			return CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer() {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), optional2));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), optional));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.of(builder.build())));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
		}

		public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource) {
			return this.killingBlow.isPresent() && !((DamageSourcePredicate)this.killingBlow.get()).matches(serverPlayer, damageSource)
				? false
				: this.entityPredicate.isEmpty() || ((ContextAwarePredicate)this.entityPredicate.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.entityPredicate.ifPresent(contextAwarePredicate -> jsonObject.add("entity", contextAwarePredicate.toJson()));
			this.killingBlow.ifPresent(damageSourcePredicate -> jsonObject.add("killing_blow", damageSourcePredicate.serializeToJson()));
			return jsonObject;
		}
	}
}
