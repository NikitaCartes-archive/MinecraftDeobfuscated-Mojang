package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
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
			super(optional);
			this.entity = optional2;
		}

		public static Criterion<TameAnimalTrigger.TriggerInstance> tamedAnimal() {
			return CriteriaTriggers.TAME_ANIMAL.createCriterion(new TameAnimalTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
		}

		public static Criterion<TameAnimalTrigger.TriggerInstance> tamedAnimal(EntityPredicate.Builder builder) {
			return CriteriaTriggers.TAME_ANIMAL.createCriterion(new TameAnimalTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder))));
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
