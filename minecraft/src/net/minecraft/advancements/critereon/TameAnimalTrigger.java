package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
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
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ContextAwarePredicate> optional2 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new TameAnimalTrigger.TriggerInstance(optional, optional2);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> entity;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional2) {
			super(TameAnimalTrigger.ID, optional);
			this.entity = optional2;
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
			return new TameAnimalTrigger.TriggerInstance(Optional.empty(), Optional.empty());
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal(Optional<EntityPredicate> optional) {
			return new TameAnimalTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional));
		}

		public boolean matches(LootContext lootContext) {
			return this.entity.isEmpty() || ((ContextAwarePredicate)this.entity.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.entity.ifPresent(contextAwarePredicate -> jsonObject.add("entity", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
