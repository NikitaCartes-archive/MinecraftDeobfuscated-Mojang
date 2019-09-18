package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DamagePredicate damagePredicate = DamagePredicate.fromJson(jsonObject.get("damage"));
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		return new PlayerHurtEntityTrigger.TriggerInstance(damagePredicate, entityPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, entity, damageSource, f, g, bl));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DamagePredicate damage;
		private final EntityPredicate entity;

		public TriggerInstance(DamagePredicate damagePredicate, EntityPredicate entityPredicate) {
			super(PlayerHurtEntityTrigger.ID);
			this.damage = damagePredicate;
			this.entity = entityPredicate;
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder builder) {
			return new PlayerHurtEntityTrigger.TriggerInstance(builder.build(), EntityPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
			return !this.damage.matches(serverPlayer, damageSource, f, g, bl) ? false : this.entity.matches(serverPlayer, entity);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("damage", this.damage.serializeToJson());
			jsonObject.add("entity", this.entity.serializeToJson());
			return jsonObject;
		}
	}
}
