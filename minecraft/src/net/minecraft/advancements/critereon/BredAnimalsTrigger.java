package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
	@Override
	public Codec<BredAnimalsTrigger.TriggerInstance> codec() {
		return BredAnimalsTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal, Animal animal2, @Nullable AgeableMob ageableMob) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
		LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, animal2);
		LootContext lootContext3 = ageableMob != null ? EntityPredicate.createContext(serverPlayer, ageableMob) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2, lootContext3));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player,
		Optional<ContextAwarePredicate> parent,
		Optional<ContextAwarePredicate> partner,
		Optional<ContextAwarePredicate> child
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<BredAnimalsTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BredAnimalsTrigger.TriggerInstance::player),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(BredAnimalsTrigger.TriggerInstance::parent),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(BredAnimalsTrigger.TriggerInstance::partner),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(BredAnimalsTrigger.TriggerInstance::child)
					)
					.apply(instance, BredAnimalsTrigger.TriggerInstance::new)
		);

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
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.parent, ".parent");
			criterionValidator.validateEntity(this.partner, ".partner");
			criterionValidator.validateEntity(this.child, ".child");
		}
	}
}
