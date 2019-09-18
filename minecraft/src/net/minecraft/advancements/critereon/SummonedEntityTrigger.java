package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("summoned_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		return new SummonedEntityTrigger.TriggerInstance(entityPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, entity));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate entity;

		public TriggerInstance(EntityPredicate entityPredicate) {
			super(SummonedEntityTrigger.ID);
			this.entity = entityPredicate;
		}

		public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder builder) {
			return new SummonedEntityTrigger.TriggerInstance(builder.build());
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity) {
			return this.entity.matches(serverPlayer, entity);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("entity", this.entity.serializeToJson());
			return jsonObject;
		}
	}
}
