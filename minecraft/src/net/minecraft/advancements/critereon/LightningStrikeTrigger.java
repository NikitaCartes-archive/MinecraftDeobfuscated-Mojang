package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("lightning_strike");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public LightningStrikeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "lightning", deserializationContext);
		ContextAwarePredicate contextAwarePredicate3 = EntityPredicate.fromJson(jsonObject, "bystander", deserializationContext);
		return new LightningStrikeTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicate2, contextAwarePredicate3);
	}

	public void trigger(ServerPlayer serverPlayer, LightningBolt lightningBolt, List<Entity> list) {
		List<LootContext> list2 = (List<LootContext>)list.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, lightningBolt);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, list2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate lightning;
		private final ContextAwarePredicate bystander;

		public TriggerInstance(
			ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate contextAwarePredicate2, ContextAwarePredicate contextAwarePredicate3
		) {
			super(LightningStrikeTrigger.ID, contextAwarePredicate);
			this.lightning = contextAwarePredicate2;
			this.bystander = contextAwarePredicate3;
		}

		public static LightningStrikeTrigger.TriggerInstance lighthingStrike(EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
			return new LightningStrikeTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), EntityPredicate.wrap(entityPredicate2));
		}

		public boolean matches(LootContext lootContext, List<LootContext> list) {
			return !this.lightning.matches(lootContext) ? false : this.bystander == ContextAwarePredicate.ANY || !list.stream().noneMatch(this.bystander::matches);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("lightning", this.lightning.toJson(serializationContext));
			jsonObject.add("bystander", this.bystander.toJson(serializationContext));
			return jsonObject;
		}
	}
}
