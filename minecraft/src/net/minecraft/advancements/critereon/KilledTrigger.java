package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
	private final ResourceLocation id;

	public KilledTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public KilledTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new KilledTrigger.TriggerInstance(
			this.id, EntityPredicate.fromJson(jsonObject.get("entity")), DamageSourcePredicate.fromJson(jsonObject.get("killing_blow"))
		);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, entity, damageSource));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate entityPredicate;
		private final DamageSourcePredicate killingBlow;

		public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
			super(resourceLocation);
			this.entityPredicate = entityPredicate;
			this.killingBlow = damageSourcePredicate;
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, builder.build(), DamageSourcePredicate.ANY);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, builder.build(), builder2.build());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
			return !this.killingBlow.matches(serverPlayer, damageSource) ? false : this.entityPredicate.matches(serverPlayer, entity);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("entity", this.entityPredicate.serializeToJson());
			jsonObject.add("killing_blow", this.killingBlow.serializeToJson());
			return jsonObject;
		}
	}
}
