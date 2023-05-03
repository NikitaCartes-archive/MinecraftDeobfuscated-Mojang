package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("tame_animal");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TameAnimalTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new TameAnimalTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate entity;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate contextAwarePredicate2) {
			super(TameAnimalTrigger.ID, contextAwarePredicate);
			this.entity = contextAwarePredicate2;
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
			return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate entityPredicate) {
			return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate));
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
