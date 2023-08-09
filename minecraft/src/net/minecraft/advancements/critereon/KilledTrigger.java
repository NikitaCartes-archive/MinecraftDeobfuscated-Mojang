package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
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
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		return new KilledTrigger.TriggerInstance(
			this.id, optional, EntityPredicate.fromJson(jsonObject, "entity", deserializationContext), DamageSourcePredicate.fromJson(jsonObject.get("killing_blow"))
		);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> entityPredicate;
		private final Optional<DamageSourcePredicate> killingBlow;

		public TriggerInstance(
			ResourceLocation resourceLocation,
			Optional<ContextAwarePredicate> optional,
			Optional<ContextAwarePredicate> optional2,
			Optional<DamageSourcePredicate> optional3
		) {
			super(resourceLocation, optional);
			this.entityPredicate = optional2;
			this.killingBlow = optional3;
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(Optional<EntityPredicate> optional) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(optional), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(builder), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), Optional.empty(), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(optional), optional2);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(builder), optional);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(optional), builder.build());
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(builder), builder2.build());
		}

		public static KilledTrigger.TriggerInstance playerKilledEntityNearSculkCatalyst() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, Optional.empty(), Optional.empty(), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(Optional<EntityPredicate> optional) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(optional), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(builder), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), Optional.empty(), Optional.empty());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(optional), optional2);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(builder), optional);
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(optional), builder.build());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(builder), builder2.build());
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
