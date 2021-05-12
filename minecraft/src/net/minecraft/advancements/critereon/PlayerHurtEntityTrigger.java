package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		DamagePredicate damagePredicate = DamagePredicate.fromJson(jsonObject.get("damage"));
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "entity", deserializationContext);
		return new PlayerHurtEntityTrigger.TriggerInstance(composite, damagePredicate, composite2);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource, f, g, bl));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DamagePredicate damage;
		private final EntityPredicate.Composite entity;

		public TriggerInstance(EntityPredicate.Composite composite, DamagePredicate damagePredicate, EntityPredicate.Composite composite2) {
			super(PlayerHurtEntityTrigger.ID, composite);
			this.damage = damagePredicate;
			this.entity = composite2;
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
			return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY, EntityPredicate.Composite.ANY);
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate damagePredicate) {
			return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, damagePredicate, EntityPredicate.Composite.ANY);
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder builder) {
			return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, builder.build(), EntityPredicate.Composite.ANY);
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(EntityPredicate entityPredicate) {
			return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY, EntityPredicate.Composite.wrap(entityPredicate));
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate damagePredicate, EntityPredicate entityPredicate) {
			return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, damagePredicate, EntityPredicate.Composite.wrap(entityPredicate));
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder builder, EntityPredicate entityPredicate) {
			return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, builder.build(), EntityPredicate.Composite.wrap(entityPredicate));
		}

		public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource, float f, float g, boolean bl) {
			return !this.damage.matches(serverPlayer, damageSource, f, g, bl) ? false : this.entity.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("damage", this.damage.serializeToJson());
			jsonObject.add("entity", this.entity.toJson(serializationContext));
			return jsonObject;
		}
	}
}
