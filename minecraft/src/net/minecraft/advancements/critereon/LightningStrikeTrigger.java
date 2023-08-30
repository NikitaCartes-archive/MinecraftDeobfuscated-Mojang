package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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
	public LightningStrikeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ContextAwarePredicate> optional2 = EntityPredicate.fromJson(jsonObject, "lightning", deserializationContext);
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "bystander", deserializationContext);
		return new LightningStrikeTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, LightningBolt lightningBolt, List<Entity> list) {
		List<LootContext> list2 = (List<LootContext>)list.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, lightningBolt);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, list2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> lightning;
		private final Optional<ContextAwarePredicate> bystander;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional2, Optional<ContextAwarePredicate> optional3) {
			super(optional);
			this.lightning = optional2;
			this.bystander = optional3;
		}

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
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.lightning.ifPresent(contextAwarePredicate -> jsonObject.add("lightning", contextAwarePredicate.toJson()));
			this.bystander.ifPresent(contextAwarePredicate -> jsonObject.add("bystander", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
