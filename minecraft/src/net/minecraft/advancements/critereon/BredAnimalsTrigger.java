package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
	public BredAnimalsTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ContextAwarePredicate> optional2 = EntityPredicate.fromJson(jsonObject, "parent", deserializationContext);
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "partner", deserializationContext);
		Optional<ContextAwarePredicate> optional4 = EntityPredicate.fromJson(jsonObject, "child", deserializationContext);
		return new BredAnimalsTrigger.TriggerInstance(optional, optional2, optional3, optional4);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal, Animal animal2, @Nullable AgeableMob ageableMob) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
		LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, animal2);
		LootContext lootContext3 = ageableMob != null ? EntityPredicate.createContext(serverPlayer, ageableMob) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2, lootContext3));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> parent;
		private final Optional<ContextAwarePredicate> partner;
		private final Optional<ContextAwarePredicate> child;

		public TriggerInstance(
			Optional<ContextAwarePredicate> optional,
			Optional<ContextAwarePredicate> optional2,
			Optional<ContextAwarePredicate> optional3,
			Optional<ContextAwarePredicate> optional4
		) {
			super(optional);
			this.parent = optional2;
			this.partner = optional3;
			this.child = optional4;
		}

		public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals() {
			return CriteriaTriggers.BRED_ANIMALS
				.createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(EntityPredicate.Builder builder) {
			return CriteriaTriggers.BRED_ANIMALS
				.createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(builder))));
		}

		public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(
			Optional<EntityPredicate> optional, Optional<EntityPredicate> optional2, Optional<EntityPredicate> optional3
		) {
			return CriteriaTriggers.BRED_ANIMALS
				.createCriterion(
					new BredAnimalsTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), EntityPredicate.wrap(optional2), EntityPredicate.wrap(optional3))
				);
		}

		public boolean matches(LootContext lootContext, LootContext lootContext2, @Nullable LootContext lootContext3) {
			return !this.child.isPresent() || lootContext3 != null && ((ContextAwarePredicate)this.child.get()).matches(lootContext3)
				? matches(this.parent, lootContext) && matches(this.partner, lootContext2) || matches(this.parent, lootContext2) && matches(this.partner, lootContext)
				: false;
		}

		private static boolean matches(Optional<ContextAwarePredicate> optional, LootContext lootContext) {
			return optional.isEmpty() || ((ContextAwarePredicate)optional.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.parent.ifPresent(contextAwarePredicate -> jsonObject.add("parent", contextAwarePredicate.toJson()));
			this.partner.ifPresent(contextAwarePredicate -> jsonObject.add("partner", contextAwarePredicate.toJson()));
			this.child.ifPresent(contextAwarePredicate -> jsonObject.add("child", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
