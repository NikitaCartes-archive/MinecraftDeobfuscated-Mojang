package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
	@Override
	public Codec<LightningStrikeTrigger.TriggerInstance> codec() {
		return LightningStrikeTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, LightningBolt lightningBolt, List<Entity> list) {
		List<LootContext> list2 = (List<LootContext>)list.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, lightningBolt);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, list2));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> lightning, Optional<ContextAwarePredicate> bystander
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<LightningStrikeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LightningStrikeTrigger.TriggerInstance::player),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("lightning").forGetter(LightningStrikeTrigger.TriggerInstance::lightning),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("bystander").forGetter(LightningStrikeTrigger.TriggerInstance::bystander)
					)
					.apply(instance, LightningStrikeTrigger.TriggerInstance::new)
		);

		public static Criterion<LightningStrikeTrigger.TriggerInstance> lightningStrike(Optional<EntityPredicate> optional, Optional<EntityPredicate> optional2) {
			return CriteriaTriggers.LIGHTNING_STRIKE
				.createCriterion(new LightningStrikeTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), EntityPredicate.wrap(optional2)));
		}

		public boolean matches(LootContext lootContext, List<LootContext> list) {
			return this.lightning.isPresent() && !((ContextAwarePredicate)this.lightning.get()).matches(lootContext)
				? false
				: !this.bystander.isPresent() || !list.stream().noneMatch(((ContextAwarePredicate)this.bystander.get())::matches);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.lightning, ".lightning");
			criterionValidator.validateEntity(this.bystander, ".bystander");
		}
	}
}
