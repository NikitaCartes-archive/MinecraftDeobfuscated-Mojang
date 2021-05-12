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
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "entity", deserializationContext);
		return new SummonedEntityTrigger.TriggerInstance(composite, composite2);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate.Composite entity;

		public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2) {
			super(SummonedEntityTrigger.ID, composite);
			this.entity = composite2;
		}

		public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder builder) {
			return new SummonedEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()));
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
