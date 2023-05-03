package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
	final ResourceLocation id;

	public KilledTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public KilledTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		return new KilledTrigger.TriggerInstance(
			this.id,
			contextAwarePredicate,
			EntityPredicate.fromJson(jsonObject, "entity", deserializationContext),
			DamageSourcePredicate.fromJson(jsonObject.get("killing_blow"))
		);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate entityPredicate;
		private final DamageSourcePredicate killingBlow;

		public TriggerInstance(
			ResourceLocation resourceLocation,
			ContextAwarePredicate contextAwarePredicate,
			ContextAwarePredicate contextAwarePredicate2,
			DamageSourcePredicate damageSourcePredicate
		) {
			super(resourceLocation, contextAwarePredicate);
			this.entityPredicate = contextAwarePredicate2;
			this.killingBlow = damageSourcePredicate;
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate entityPredicate) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()), DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity() {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), damageSourcePredicate
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate damageSourcePredicate) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()), damageSourcePredicate
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate entityPredicate, DamageSourcePredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), builder.build()
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()), builder2.build()
			);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntityNearSculkCatalyst() {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate entityPredicate) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()), DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer() {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, DamageSourcePredicate.ANY
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), damageSourcePredicate
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate damageSourcePredicate) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()), damageSourcePredicate
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate entityPredicate, DamageSourcePredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), builder.build()
			);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return new KilledTrigger.TriggerInstance(
				CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()), builder2.build()
			);
		}

		public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource) {
			return !this.killingBlow.matches(serverPlayer, damageSource) ? false : this.entityPredicate.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("entity", this.entityPredicate.toJson(serializationContext));
			jsonObject.add("killing_blow", this.killingBlow.serializeToJson());
			return jsonObject;
		}
	}
}
