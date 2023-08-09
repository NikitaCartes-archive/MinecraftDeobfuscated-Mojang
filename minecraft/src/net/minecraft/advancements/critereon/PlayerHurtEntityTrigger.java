package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public PlayerHurtEntityTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<DamagePredicate> optional2 = DamagePredicate.fromJson(jsonObject.get("damage"));
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new PlayerHurtEntityTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource, f, g, bl));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<DamagePredicate> damage;
		private final Optional<ContextAwarePredicate> entity;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<DamagePredicate> optional2, Optional<ContextAwarePredicate> optional3) {
			super(PlayerHurtEntityTrigger.ID, optional);
			this.damage = optional2;
			this.entity = optional3;
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
			return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty());
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntityWithDamage(Optional<DamagePredicate> optional) {
			return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), optional, Optional.empty());
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntityWithDamage(DamagePredicate.Builder builder) {
			return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), builder.build(), Optional.empty());
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(Optional<EntityPredicate> optional) {
			return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap(optional));
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(Optional<DamagePredicate> optional, Optional<EntityPredicate> optional2) {
			return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), optional, EntityPredicate.wrap(optional2));
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder builder, Optional<EntityPredicate> optional) {
			return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), builder.build(), EntityPredicate.wrap(optional));
		}

		public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource, float f, float g, boolean bl) {
			return this.damage.isPresent() && !((DamagePredicate)this.damage.get()).matches(serverPlayer, damageSource, f, g, bl)
				? false
				: !this.entity.isPresent() || ((ContextAwarePredicate)this.entity.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.damage.ifPresent(damagePredicate -> jsonObject.add("damage", damagePredicate.serializeToJson()));
			this.entity.ifPresent(contextAwarePredicate -> jsonObject.add("entity", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
