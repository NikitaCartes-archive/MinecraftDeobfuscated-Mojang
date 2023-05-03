package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("summoned_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public SummonedEntityTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new SummonedEntityTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate entity;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate contextAwarePredicate2) {
			super(SummonedEntityTrigger.ID, contextAwarePredicate);
			this.entity = contextAwarePredicate2;
		}

		public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder builder) {
			return new SummonedEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build()));
		}

		public boolean matches(LootContext lootContext) {
			return this.entity.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("entity", this.entity.toJson(serializationContext));
			return jsonObject;
		}
	}
}
